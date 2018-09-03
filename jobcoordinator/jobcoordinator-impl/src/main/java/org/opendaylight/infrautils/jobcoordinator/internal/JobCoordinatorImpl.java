/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinator;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinatorMonitor;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.utils.concurrent.JdkFutures;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.opendaylight.infrautils.utils.concurrent.LoggingUncaughtThreadDeathContextRunnable;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = {JobCoordinator.class, JobCoordinatorMonitor.class })
public class JobCoordinatorImpl implements JobCoordinator, JobCoordinatorMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(JobCoordinatorImpl.class);

    private static final long RETRY_WAIT_BASE_TIME_MILLIS = 1000;

    private static final int FJP_MAX_CAP = 0x7fff; // max #workers - 1; copy/pasted from ForkJoinPool private
    private final ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
        ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("jobcoordinator-main-task-" + worker.getPoolIndex());
        return worker;
    };
    private final ForkJoinPool fjPool = new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()), factory,
            LoggingThreadUncaughtExceptionHandler.toLogger(LOG), false);

    private final Map<String, JobQueue> jobQueueMap = new ConcurrentHashMap<>();
    private final ReentrantLock jobQueueMapLock = new ReentrantLock();
    private final Condition jobQueueMapCondition = jobQueueMapLock.newCondition();

    private final Meter jobsCreated;
    private final Meter jobsCleared;
    private final Counter jobsPending;
    private final Counter jobsIncomplete;
    private final Meter jobsFailed;
    private final Meter jobsRetriesForFailure;
    private final Meter jobExecuteAttempts;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5,
            ThreadFactoryProvider.builder().namePrefix("jobcoordinator-onfailure-executor").logger(LOG).build().get());

    private final Thread jobQueueHandlerThread;
    private final AtomicBoolean jobQueueHandlerThreadStarted = new AtomicBoolean(false);

    @GuardedBy("jobQueueMapLock")
    private boolean isJobAvailable = false;

    private volatile boolean shutdown = false;

    @Inject
    public JobCoordinatorImpl(@Reference MetricProvider metricProvider) {
        jobsCreated = metricProvider.newMeter(this, "odl.infrautils.jobcoordinator.jobsCreated");
        jobsCleared = metricProvider.newMeter(this, "odl.infrautils.jobcoordinator.jobsCleared");
        jobsPending = metricProvider.newCounter(this, "odl.infrautils.jobcoordinator.jobsPending");
        jobsIncomplete = metricProvider.newCounter(this, "odl.infrautils.jobcoordinator.jobsIncomplete");
        jobsFailed = metricProvider.newMeter(this, "odl.infrautils.jobcoordinator.jobsFailed");
        jobsRetriesForFailure = metricProvider.newMeter(this, "odl.infrautils.jobcoordinator.jobsRetriesForFailure");
        jobExecuteAttempts = metricProvider.newMeter(this, "odl.infrautils.jobcoordinator.jobExecuteAttempts");

        jobQueueHandlerThread = ThreadFactoryProvider.builder()
            .namePrefix("JobCoordinator-JobQueueHandler")
            .logger(LOG)
            .build().get()
            .newThread(new JobQueueHandler());
    }

    @PreDestroy
    public void destroy() {
        LOG.info("JobCoordinator shutting down... (tasks still running may be stopped/cancelled/interrupted)");

        jobQueueMapLock.lock();
        try {
            shutdown = true;
            jobQueueMapCondition.signalAll();
        } finally {
            jobQueueMapLock.unlock();
        }

        fjPool.shutdownNow();
        scheduledExecutorService.shutdownNow();

        try {
            jobQueueHandlerThread.join(10000);
        } catch (InterruptedException e) {
            // Shouldn't get interrupted - either way we don't care.
        }

        LOG.info("JobCoordinator now closed for business.");
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker) {
        enqueueJob(key, mainWorker, null, JobCoordinator.DEFAULT_MAX_RETRIES);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker,
            RollbackCallable rollbackWorker) {
        enqueueJob(key, mainWorker, rollbackWorker, JobCoordinator.DEFAULT_MAX_RETRIES);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, int maxRetries) {
        enqueueJob(key, mainWorker, null, maxRetries);
    }

    @Override
    public void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker,
            @Nullable RollbackCallable rollbackWorker, int maxRetries) {
        JobEntry jobEntry = new JobEntry(key, mainWorker, rollbackWorker, maxRetries);
        JobQueue jobQueue = jobQueueMap.computeIfAbsent(key, mapKey -> new JobQueue());
        jobQueue.addEntry(jobEntry);

        jobsPending.increment();
        jobsIncomplete.increment();
        jobsCreated.mark();

        signalForNextJob();
    }

    @Override
    public long getClearedTaskCount() {
        return jobsCleared.get();
    }

    @Override
    public long getCreatedTaskCount() {
        return jobsCreated.get();
    }

    @Override
    public long getIncompleteTaskCount() {
        return jobsIncomplete.get();
    }

    @Override
    public long getPendingTaskCount() {
        return jobsPending.get();
    }

    @Override
    public long getFailedJobCount() {
        return jobsFailed.get();
    }

    @Override
    public long getRetriesCount() {
        return jobsRetriesForFailure.get();
    }

    @Override
    public long getExecuteAttempts() {
        return jobExecuteAttempts.get();
    }

    /**
     * Cleanup the submitted job from the job queue.
     **/
    private void clearJob(JobEntry jobEntry) {
        String jobKey = jobEntry.getKey();
        LOG.trace("About to clear jobKey: {}", jobKey);
        JobQueue jobQueue = jobQueueMap.get(jobKey);
        if (jobQueue != null) {
            jobQueue.setExecutingEntry(null);
        } else {
            LOG.error("clearJob: jobQueueMap did not contain the key for this entry: {}", jobEntry);
        }
        jobsCleared.mark();
        jobsIncomplete.decrement();
        signalForNextJob();
    }

    private void signalForNextJob() {
        if (jobQueueHandlerThreadStarted.compareAndSet(false, true)) {
            jobQueueHandlerThread.start();
        }

        jobQueueMapLock.lock();
        try {
            isJobAvailable = true;
            jobQueueMapCondition.signalAll();
        } finally {
            jobQueueMapLock.unlock();
        }
    }

    private boolean executeTask(Runnable task) {
        try {
            fjPool.execute(task);
            return true;
        } catch (RejectedExecutionException e) {
            if (!fjPool.isShutdown()) {
                LOG.error("ForkJoinPool task rejected", e);
            }

            return false;
        }
    }

    private Future<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        try {
            return scheduledExecutorService.schedule(task, delay, unit);
        } catch (RejectedExecutionException e) {
            if (!scheduledExecutorService.isShutdown()) {
                LOG.error("ScheduledExecutorService rejected task", e);
            }
            return immediateFailedFuture(e);
        }
    }

    @VisibleForTesting
    // Ideally this would be package-scoped but the unit test class is in a separate package.
    protected Thread getJobQueueHandlerThread() {
        return jobQueueHandlerThread;
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
        public void onSuccess(@Nullable List<Void> voids) {
            LOG.trace("Job completed successfully: {}", jobEntry.getKey());
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
            int retryCount = jobEntry.decrementRetryCountAndGet();
            jobsRetriesForFailure.mark();

            if (retryCount == 0 && jobEntry.getMaxRetries() > 0) {
                LOG.error("Job still failed on final retry: {}", jobEntry, throwable);
            } else if (retryCount == 0 && jobEntry.getMaxRetries() == 0) {
                LOG.error("Job failed, no retries: {}", jobEntry, throwable);
            } else {
                // If retryCount > 0, then the log should not be polluted with confusing WARN messages (because we're
                // about to retry it again, shortly; if it ultimately still fails, there will be a WARN); so DEBUG.
                LOG.debug("Job failed, will retry: {}", jobEntry, throwable);
            }
            if (jobEntry.getMainWorker() == null) {
                LOG.error("Job failed with Double-Fault. Bailing Out: {}", jobEntry);
                clearJob(jobEntry);
                return;
            }

            if (retryCount > 0) {
                long waitTime = RETRY_WAIT_BASE_TIME_MILLIS / retryCount;
                Futures.addCallback(JdkFutures.toListenableFuture(scheduleTask(() -> {
                    MainTask worker = new MainTask(jobEntry);
                    executeTask(worker);
                }, waitTime, TimeUnit.MILLISECONDS)), new FutureCallback<Object>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        LOG.error("Retry of job failed; rolling back or clearing job: {}", jobEntry, throwable);
                        rollbackOrClear(jobEntry);
                    }

                    @Override
                    public void onSuccess(Object result) {
                        LOG.debug("Retry of job succeeded: {}", jobEntry);
                    }
                }, MoreExecutors.directExecutor());
            } else {
                rollbackOrClear(jobEntry);
            }
        }
    }

    private void rollbackOrClear(JobEntry jobEntry) {
        jobsFailed.mark();
        if (jobEntry.getRollbackWorker() != null) {
            jobEntry.setMainWorker(null);
            RollbackTask rollbackTask = new RollbackTask(jobEntry);
            executeTask(rollbackTask);
            return;
        }
        clearJob(jobEntry);
    }

    /**
     * RollbackTask is used to execute the RollbackCallable provided by the
     * application in the eventuality of a failure.
     */
    private class RollbackTask extends LoggingUncaughtThreadDeathContextRunnable {
        private final JobEntry jobEntry;

        RollbackTask(JobEntry jobEntry) {
            super(LOG, jobEntry::toString);
            this.jobEntry = jobEntry;
        }

        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        public void runWithUncheckedExceptionLogging() {
            RollbackCallable rollbackWorker = jobEntry.getRollbackWorker();
            @Var List<ListenableFuture<Void>> futures = null;
            if (rollbackWorker != null) {
                try {
                    futures = rollbackWorker.apply(jobEntry.getFutures());
                } catch (Exception e) {
                    LOG.error("Exception when executing job's rollbackWorker: {}", jobEntry, e);
                }
            } else {
                LOG.error("Unexpected no (null) rollback worker on job: {}", jobEntry);
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            jobEntry.setFutures(futures);
            ListenableFuture<List<Void>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry), MoreExecutors.directExecutor());
        }
    }

    /**
     * Execute the MainWorker callable.
     */
    private class MainTask extends LoggingUncaughtThreadDeathContextRunnable {
        private static final int LONG_JOBS_THRESHOLD_MS = 1000;
        private final JobEntry jobEntry;

        MainTask(JobEntry jobEntry) {
            super(LOG, jobEntry::toString);
            this.jobEntry = jobEntry;
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void runWithUncheckedExceptionLogging() {
            @Var List<ListenableFuture<Void>> futures = null;
            long jobStartTimestampNanos = System.nanoTime();
            LOG.trace("Running job with key: {}", jobEntry.getKey());

            try {
                Callable<List<ListenableFuture<Void>>> mainWorker = jobEntry.getMainWorker();
                if (mainWorker != null) {
                    futures = mainWorker.call();
                } else {
                    LOG.error("Unexpected no (null) main worker on job: {}", jobEntry);
                }
                long jobExecutionTimeNanos = System.nanoTime() - jobStartTimestampNanos;
                printJobs(jobEntry.getKey(), TimeUnit.NANOSECONDS.toMillis(jobExecutionTimeNanos));
            } catch (Exception e) {
                jobsFailed.mark();
                LOG.error("Direct Exception (not failed Future) when executing job, won't even retry: {}", jobEntry, e);
            }

            if (futures == null || futures.isEmpty()) {
                clearJob(jobEntry);
                return;
            }

            jobEntry.setFutures(futures);
            ListenableFuture<List<Void>> listenableFuture = Futures.allAsList(futures);
            Futures.addCallback(listenableFuture, new JobCallback(jobEntry), MoreExecutors.directExecutor());
        }

        private void printJobs(String key, long jobExecutionTime) {
            if (jobExecutionTime > LONG_JOBS_THRESHOLD_MS) {
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
                    for (Map.Entry<String, JobQueue> entry : jobQueueMap.entrySet()) {
                        if (shutdown) {
                            break;
                        }

                        JobQueue jobQueue = entry.getValue();
                        if (jobQueue.getExecutingEntry() != null) {
                            jobExecuteAttempts.mark();
                            continue;
                        }
                        JobEntry jobEntry = jobQueue.poll();
                        if (jobEntry == null) {
                            // job queue is empty. so continue with next job queue entry
                            continue;
                        }
                        jobQueue.setExecutingEntry(jobEntry);
                        MainTask worker = new MainTask(jobEntry);
                        LOG.trace("Executing job with key: {}", jobEntry.getKey());

                        if (executeTask(worker)) {
                            jobsPending.decrement();
                        }
                    }

                    if (!waitForJobIfNeeded()) {
                        break;
                    }
                } catch (Exception e) {
                    LOG.error("Exception while executing the tasks", e);
                }
            }
        }

        // Suppress "Exceptional return value of java.util.concurrent.locks.Condition.await" - we really don't care
        // if the Condition was signaled or timed out as we use isJobAvailable to break or continue waiting.
        @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
        private boolean waitForJobIfNeeded() throws InterruptedException {
            jobQueueMapLock.lock();
            try {
                while (!isJobAvailable && !shutdown) {
                    jobQueueMapCondition.await(1, TimeUnit.SECONDS);
                }
                isJobAvailable = false;
                return !shutdown;
            } finally {
                jobQueueMapLock.unlock();
            }
        }
    }

    @Override
    public String toString() {
        boolean isJobAvailableFromLock;
        jobQueueMapLock.lock();
        try {
            isJobAvailableFromLock = isJobAvailable;
        } finally {
            jobQueueMapLock.unlock();
        }

        return MoreObjects.toStringHelper(this).add("incompleteTasks", getIncompleteTaskCount())
                .add("pendingTasks", getPendingTaskCount()).add("failedJobs", getFailedJobCount())
                .add("clearedTasks", getClearedTaskCount()).add("createdTasks", getCreatedTaskCount())
                .add("executeAttempts", getExecuteAttempts()).add("retriesCount", getRetriesCount())
                .add("fjPool", fjPool).add("jobQueueMap", jobQueueMap).add("jobQueueMapLock", jobQueueMapLock)
                .add("scheduledExecutorService", scheduledExecutorService).add("isJobAvailable", isJobAvailableFromLock)
                .add("jobQueueMapCondition", jobQueueMapCondition)
                .toString();
    }
}
