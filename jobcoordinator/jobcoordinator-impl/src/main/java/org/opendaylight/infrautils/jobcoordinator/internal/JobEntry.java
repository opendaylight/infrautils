/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import static java.util.Collections.emptyList;

import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
final class JobEntry {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private final String id = "J" + ID_GENERATOR.getAndIncrement();
    private final Object key;
    private final String queueId;
    private volatile @Nullable Callable<List<? extends ListenableFuture<?>>> mainWorker;
    private final @Nullable RollbackCallable rollbackWorker;
    private final int maxRetries;
    private volatile int retryCount;
    private static final AtomicIntegerFieldUpdater<JobEntry> RETRY_COUNT_FIELD_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(JobEntry.class, "retryCount");
    private volatile @Nullable List<? extends ListenableFuture<?>> futures;
    private long startTime = -1;
    private long endTime = -1;


    @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "TYPE_USE and SpotBugs")
    JobEntry(Object key, String queueId, Callable<List<? extends ListenableFuture<?>>> mainWorker,
             @Nullable RollbackCallable rollbackWorker,
            int maxRetries) {
        this.key = key;
        this.queueId = queueId;
        this.mainWorker = mainWorker;
        this.rollbackWorker = rollbackWorker;
        this.maxRetries = maxRetries;
        this.retryCount = maxRetries;
    }

    /**
     * Get the key provided by the application that segregates the callables
     * that can be run parallely. NOTE: Currently, this is a string. Can be
     * converted to Object where Object implementation should provide the
     * hashcode and equals methods.
     */
    Object getKey() {
        return key;
    }

    String getId() {
        return id;
    }

    String getQueueId() {
        return queueId;
    }

    @Nullable Callable<List<? extends ListenableFuture<?>>> getMainWorker() {
        return mainWorker;
    }

    void setMainWorker(@Nullable Callable<List<? extends ListenableFuture<?>>> mainWorker) {
        this.mainWorker = mainWorker;
    }

    @Nullable RollbackCallable getRollbackWorker() {
        return rollbackWorker;
    }

    int getRetryCount() {
        return retryCount;
    }

    int getMaxRetries() {
        return maxRetries;
    }

    int decrementRetryCountAndGet() {
        if (this.retryCount == 0) {
            return 0;
        }

        return RETRY_COUNT_FIELD_UPDATER.decrementAndGet(this);
    }

    public List<? extends ListenableFuture<?>> getFutures() {
        List<? extends ListenableFuture<?>> nullableFutures = futures;
        return nullableFutures != null ? nullableFutures : emptyList();
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        if (this.startTime < 0) {
            this.startTime = startTime;
        }
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setFutures(List<? extends ListenableFuture<?>> futures) {
        this.futures = futures;
    }



    @Override
    public String toString() {

        return "JobEntry{"
                + "key='" + key + '\''
                + ", jobId='" + id + '\''
                + ", queueId='" + queueId + '\''
                + ", mainWorker=" + mainWorker
                + ", rollbackWorker=" + rollbackWorker
                + ", retryCount=" + (maxRetries - retryCount) + "/" + maxRetries
                + ", futures=" + futures
                + '}';

    }
}
