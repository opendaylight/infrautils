/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * JobEntry is the entity built per job submitted by the application and
 * enqueued to the book-keeping data structure.
 */
class JobEntry<T> {

    private final String key;
    private volatile @Nullable Callable<List<ListenableFuture<T>>> mainWorker;
    private final @Nullable RollbackCallable rollbackWorker;
    private final int maxRetries;
    private volatile int retryCount;
    private static final AtomicIntegerFieldUpdater<JobEntry> RETRY_COUNT_FIELD_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(JobEntry.class, "retryCount");
    private volatile @Nullable List<ListenableFuture<T>> futures;

    JobEntry(String key, Callable<List<ListenableFuture<T>>> mainWorker, @Nullable RollbackCallable rollbackWorker,
            int maxRetries) {
        this.key = key;
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
    public String getKey() {
        return key;
    }

    public @Nullable Callable<List<ListenableFuture<T>>> getMainWorker() {
        return mainWorker;
    }

    public void setMainWorker(@Nullable Callable<List<ListenableFuture<T>>> mainWorker) {
        this.mainWorker = mainWorker;
    }

    public @Nullable RollbackCallable getRollbackWorker() {
        return rollbackWorker;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int decrementRetryCountAndGet() {
        if (this.retryCount == 0) {
            return 0;
        }

        return RETRY_COUNT_FIELD_UPDATER.decrementAndGet(this);
    }

    public List<ListenableFuture<T>> getFutures() {
        List<ListenableFuture<T>> nullableFutures = futures;
        if (nullableFutures != null) {
            return nullableFutures;
        } else {
            this.futures = Collections.singletonList(SettableFuture.create());
            return futures;
        }
    }

    public void setFutures(List<ListenableFuture<T>> futures) {
        this.futures = futures;
    }

    @Override
    public String toString() {
        return "JobEntry{" + "key='" + key + '\'' + ", mainWorker=" + mainWorker + ", rollbackWorker=" + rollbackWorker
                + ", retryCount=" + (maxRetries - retryCount) + "/" + maxRetries + ", futures=" + futures + '}';
    }
}
