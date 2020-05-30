/*
 * Copyright (c) 2017 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This interface defines methods for a JobCoordinator which enables executing
 * jobs in a parallel/sequential fashion based on their keys.
 *
 * <p>Jobs will be retried if any of the Futures returned from the Callable task have failed
 * (Future contains an Exception).  However if the Callable throws any Exception
 * (which is not the same as as returning a failed Future containing an Exception),
 * then there will be no retries.
 *
 * <p>
 * Enqueued jobs are stored in unbounded queues until they are run, this should
 * be kept in mind as it might lead to an OOM.
 */
public interface JobCoordinator { // do *NOT* extends JobCoordinatorMonitor

    int DEFAULT_MAX_RETRIES = 3;

    /**
     * Enqueues a job with DEFAULT_MAX_RETRIES (3) retries.
     * See class level documentation above for details re. the retry strategy.
     *
     * @param key
     *            The job's key. Jobs with the same key are run sequentially.
     *            Jobs with different keys are run in parallel.
     * @param mainWorker
     *            The task that runs for the job.
     */
    default void enqueueJob(String key, Callable<List<? extends ListenableFuture<?>>> mainWorker) {
        enqueueJob(key, mainWorker, null, DEFAULT_MAX_RETRIES);
    }

    /**
     * Enqueues a job with a rollback task and DEFAULT_MAX_RETRIES (3) retries..
     * See class level documentation above for details re. the retry strategy.
     *
     * @param rollbackWorker
     *            The rollback task which runs in case the job's main task
     *            fails.
     * @see JobCoordinator#enqueueJob(String, Callable)
     */
    default void enqueueJob(String key, Callable<List<? extends ListenableFuture<?>>> mainWorker,
            RollbackCallable rollbackWorker) {
        enqueueJob(key, mainWorker, rollbackWorker, DEFAULT_MAX_RETRIES);
    }

    /**
     * Enqueues a job with max retries. In case the job's main task fails, it
     * will be retried until it succeeds or the specified maximum number of
     * retries has been reached.
     * See class level documentation above for details re. the retry strategy.
     *
     * @param maxRetries
     *            The maximum number of retries for the job's main task until it
     *            succeeds.
     * @see JobCoordinator#enqueueJob(String, Callable)
     */
    default void enqueueJob(String key, Callable<List<? extends ListenableFuture<?>>> mainWorker, int maxRetries) {
        enqueueJob(key, mainWorker, null, maxRetries);
    }

    /**
     * Enqueues a job with a rollback task and max retries.
     * See class level documentation above for details re. the retry strategy.
     *
     * @param rollbackWorker
     *            The rollback task which runs in case the job's main task
     *            fails.
     * @param maxRetries
     *            The maximum number of retries for the job's main task until it
     *            succeeds.
     * @see JobCoordinator#enqueueJob(String, Callable, RollbackCallable)
     * @see JobCoordinator#enqueueJob(String, Callable, int)
     */
    void enqueueJob(String key, Callable<List<? extends ListenableFuture<?>>> mainWorker,
            RollbackCallable rollbackWorker, int maxRetries);
}
