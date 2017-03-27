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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinator;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobCoordinatorImpl implements JobCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(JobCoordinatorImpl.class);

    private static final long RETRY_WAIT_BASE_TIME_MILLIS = 1000;

    private static final int FJP_MAX_CAP = 0x7fff; // max #workers - 1; copy/pasted from ForkJoinPool private

    private final ForkJoinPool fjPool = new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            LoggingThreadUncaughtExceptionHandler.toLogger(LOG),
            false);

    private final Map<String, JobQueue> jobQueueMap = new HashMap<>();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    public JobCoordinatorImpl() {
        ThreadFactoryProvider.builder()
            .namePrefix("JobCoordinator-JobQueueHandler")
            .logger(LOG)
            .build().get()
            .newThread(new JobQueueHandler()).start();
    }

    @PreDestroy
    public void destroy() {
        fjPool.shutdownNow();
        scheduledExecutorService.shutdownNow();
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

        boolean queueCreated = false;
        synchronized (jobQueueMap) {
            JobQueue jobQueue = jobQueueMap.getOrDefault(key, null);
            if (jobQueue == null) {
                queueCreated = true;
                jobQueue = new JobQueue();
                jobQueueMap.put(key, jobQueue);
            }
            jobQueue.addEntry(jobEntry);

            JobCoordinatorCounters.jobs_pending.inc();
            JobCoordinatorCounters.jobs_incomplete.inc();
            JobCoordinatorCounters.jobs_created.inc();

            if (queueCreated) {
                jobQueueMap.notify();
            }
        }
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

    /**
     * Cleanup the submitted job from the job queue.
     **/
    private void clearJob(JobEntry jobEntry) {
        LOG.trace("About to clear jobkey {}", jobEntry.getKey());
        synchronized (jobQueueMap) {
            JobQueue jobQueue = jobQueueMap.get(jobEntry.getKey());
            jobQueue.setExecutingEntry(null);
            if (jobQueue.isEmpty()) {
                LOG.trace("Clear jobkey {}", jobEntry.getKey());
                jobQueueMap.remove(jobEntry.getKey());
            }

            jobQueueMap.notify();
        }
        JobCoordinatorCounters.jobs_cleared.inc();
        JobCoordinatorCounters.jobs_incomplete.dec();
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
            if (retryCount > 0) {
                long waitTime = RETRY_WAIT_BASE_TIME_MILLIS / retryCount;
                scheduledExecutorService.schedule(() -> {
                    MainTask worker = new MainTask(jobEntry);
                    fjPool.execute(worker);
                }, waitTime, TimeUnit.MILLISECONDS);
                return;
            }

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
                    synchronized (jobQueueMap) {
                        Iterator<Map.Entry<String, JobQueue>> it = jobQueueMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, JobQueue> entry = it.next();
                            if (entry.getValue().getExecutingEntry() != null) {
                                continue;
                            }
                            JobEntry jobEntry = entry.getValue().poll();
                            if (jobEntry != null) {
                                entry.getValue().setExecutingEntry(jobEntry);
                                MainTask worker = new MainTask(jobEntry);
                                LOG.trace("Executing job {}", jobEntry.getKey());
                                fjPool.execute(worker);
                                JobCoordinatorCounters.jobs_pending.dec();

                            } else {
                                it.remove();
                            }
                        }

                        jobQueueMap.wait();
                    }
                } catch (Exception e) {
                    LOG.error("Exception while executing the tasks", e);
                }
            }
        }
    }
}
