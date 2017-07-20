/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinator;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinatorMonitor;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobCoordinatorImpl implements JobCoordinator, JobCoordinatorMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(JobCoordinatorImpl.class);

    private static final long RETRY_WAIT_BASE_TIME_MILLIS = 1000;

    private static final int FJP_MAX_CAP = 0x7fff; // max #workers - 1; copy/pasted from ForkJoinPool private

    private final ForkJoinPool fjPool = new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            LoggingThreadUncaughtExceptionHandler.toLogger(LOG),
            false);

    private final Map<String, JobQueue> jobQueueMap = new ConcurrentHashMap<>();
    private final ReentrantLock jobQueueMapLock = new ReentrantLock();
    private final Condition jobQueueMapCondition = jobQueueMapLock.newCondition();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    @GuardedBy("jobQueueMapLock")
    private boolean isJobAvailable = false;

    public JobCoordinatorImpl() {
        ThreadFactoryProvider.builder()
            .namePrefix("JobCoordinator-JobQueueHandler")
            .logger(LOG)
            .build().get()
            .newThread(new JobQueueHandler()).start();
    }

    @PreDestroy
    public void destroy() {
        LOG.info("JobCoordinator shutting down... (tasks still running may be stopped/cancelled/interrupted)");
        fjPool.shutdownNow();
        scheduledExecutorService.shutdownNow();
        LOG.info("JobCoordinator now closed for business.");
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker) {
        enqueueJob(key, mainWorker, null, 0);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker,
            RollbackCallable rollbackWorker) {
        enqueueJob(key, mainWorker, rollbackWorker, 0);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, int maxRetries) {
        enqueueJob(key, mainWorker, null, maxRetries);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker,
            RollbackCallable rollbackWorker, int maxRetries) {
        JobEntry jobEntry = new JobEntry(key, mainWorker, rollbackWorker, maxRetries);
        JobQueue jobQueue = jobQueueMap.computeIfAbsent(key, mapKey -> new JobQueue());
        jobQueue.addEntry(jobEntry);

        JobCoordinatorCounters.jobs_pending.inc();
        JobCoordinatorCounters.jobs_incomplete.inc();
        JobCoordinatorCounters.jobs_created.inc();

        signalForNexJob();
    }

    @Override
    public long getClearedTaskCount() {
        return JobCoordinatorCounters.jobs_cleared.get();
    }

    @Override
    public long getCreatedTaskCount() {
        return JobCoordinatorCounters.jobs_created.get();
    }

    @Override
    public long getIncompleteTaskCount() {
        return JobCoordinatorCounters.jobs_incomplete.get();
    }

    @Override
    public long getPendingTaskCount() {
        return JobCoordinatorCounters.jobs_pending.get();
    }

    @Override
    public long getFailedJobCount() {
        return JobCoordinatorCounters.jobs_failed.get();
    }

    @Override
    public long getRetriesCount() {
        return JobCoordinatorCounters.jobs_retries_for_failure.get();
    }

    @Override
    public long getExecuteAttempts() {
        return JobCoordinatorCounters.job_execute_attempts.get();
    }

    /**
     * Cleanup the submitted job from the job queue.
     **/
    private void clearJob(JobEntry jobEntry) {
        String jobKey = jobEntry.getKey();
        LOG.trace("About to clear jobkey {}", jobKey);
        JobQueue jobQueue = jobQueueMap.get(jobKey);
        jobQueue.setExecutingEntry(null);
        JobCoordinatorCounters.jobs_cleared.inc();
        JobCoordinatorCounters.jobs_incomplete.dec();
        signalForNexJob();
    }

    private void signalForNexJob() {
        jobQueueMapLock.lock();
        try {
            isJobAvailable = true;
            jobQueueMapCondition.signalAll();
        } finally {
            jobQueueMapLock.unlock();
        }
    }

    /**
     * JobCallback class is used as a future callback for main and rollback
     * workers to handle success and failure.
     */
    private class JobCallback implements FutureCallback<List<Void>> {
        private final JobEntry jobEntry;

        JobCallback(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        /**
         * This implies that all the future instances have returned success. --
         * TODO: Confirm this
         */
        @Override
        public void onSuccess(List<Void> voids) {
            LOG.trace("Job {} completed successfully", jobEntry.getKey());
            clearJob(jobEntry);
        }

        /**
         * This method is used to handle failure callbacks. If more retry
         * needed, the retrycount is decremented and mainworker is executed
         * again. After retries completed, rollbackworker is executed. If
         * rollbackworker fails, this is a double-fault. Double fault is logged
         * and ignored.
         */
        @Override
        public void onFailure(Throwable throwable) {
            LOG.warn("Job: {} failed", jobEntry, throwable);
            if (jobEntry.getMainWorker() == null) {
                LOG.error("Job: {} failed with Double-Fault. Bailing Out.", jobEntry);
                clearJob(jobEntry);
                return;
            }

            int retryCount = jobEntry.decrementRetryCountAndGet();
            JobCoordinatorCounters.jobs_retries_for_failure.inc();
            if (retryCount > 0) {
                long waitTime = RETRY_WAIT_BASE_TIME_MILLIS / retryCount;
                scheduledExecutorService.schedule(() -> {
                    MainTask worker = new MainTask(jobEntry);
                    fjPool.execute(worker);
                }, waitTime, TimeUnit.MILLISECONDS);
                return;
            }
            JobCoordinatorCounters.jobs_failed.inc();
            if (jobEntry.getRollbackWorker() != null) {
                jobEntry.setMainWorker(null);
                RollbackTask rollbackTask = new RollbackTask(jobEntry);
                fjPool.execute(rollbackTask);
                return;
            }

            clearJob(jobEntry);
        }
    }

    /**
     * RollbackTask is used to execute the RollbackCallable provided by the
     * application in the eventuality of a failure.
     */
    private class RollbackTask implements Runnable {
        private final JobEntry jobEntry;

        RollbackTask(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        public void run() {
            RollbackCallable callable = jobEntry.getRollbackWorker();
            callable.setFutures(jobEntry.getFutures());
            List<ListenableFuture<Void>> futures = null;

            try {
                futures = callable.call();
            } catch (Exception e) {
                LOG.error("Exception when executing jobEntry: {}", jobEntry, e);
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            jobEntry.setFutures(futures);
            ListenableFuture<List<Void>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry));
        }
    }

    /**
     * Execute the MainWorker callable.
     */
    private class MainTask implements Runnable {
        private static final int LONG_JOBS_THRESHOLD = 1000; // MS
        private final JobEntry jobEntry;

        MainTask(JobEntry jobEntry) {
            this.jobEntry = jobEntry;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            List<ListenableFuture<Void>> futures = null;
            long jobStartTimestampNanos = System.nanoTime();
            LOG.trace("Running job {}", jobEntry.getKey());

            try {
                futures = jobEntry.getMainWorker().call();
                long jobExecutionTimeNanos = System.nanoTime() - jobStartTimestampNanos;
                printJobs(jobEntry.getKey(), TimeUnit.NANOSECONDS.toMillis(jobExecutionTimeNanos));
            } catch (Throwable e) {
                JobCoordinatorCounters.jobs_failed.inc();
                LOG.error("Exception when executing jobEntry: {}", jobEntry, e);
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            jobEntry.setFutures(futures);
            ListenableFuture<List<Void>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry));
        }

        private void printJobs(String key, long jobExecutionTime) {
            if (jobExecutionTime > LONG_JOBS_THRESHOLD) {
                LOG.warn("Job {} took {}ms to complete", jobEntry.getKey(), jobExecutionTime);
                return;
            }
            LOG.trace("Job {} took {}ms to complete", jobEntry.getKey(), jobExecutionTime);
        }
    }

    private class JobQueueHandler implements Runnable {
        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void run() {
            LOG.info("Starting JobQueue Handler Thread");
            while (true) {
                try {
                    Iterator<Map.Entry<String, JobQueue>> it = jobQueueMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, JobQueue> entry = it.next();
                        JobQueue jobQueue = entry.getValue();
                        if (jobQueue.getExecutingEntry() != null) {
                            JobCoordinatorCounters.job_execute_attempts.inc();
                            continue;
                        }
                        JobEntry jobEntry = jobQueue.poll();
                        if (jobEntry == null) {
                            // job queue is empty. so continue with next job queue entry
                            continue;
                        }
                        jobQueue.setExecutingEntry(jobEntry);
                        MainTask worker = new MainTask(jobEntry);
                        LOG.trace("Executing job {}", jobEntry.getKey());
                        fjPool.execute(worker);
                        JobCoordinatorCounters.jobs_pending.dec();
                    }
                    waitForJobIfNeeded();
                } catch (Exception e) {
                    LOG.error("Exception while executing the tasks", e);
                }
            }
        }

        private void waitForJobIfNeeded() throws InterruptedException {
            jobQueueMapLock.lock();
            try {
                while (!isJobAvailable) {
                    jobQueueMapCondition.await(1, TimeUnit.SECONDS);
                }
                isJobAvailable = false;
            } finally {
                jobQueueMapLock.unlock();
            }
        }
    }
}
