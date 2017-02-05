/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.datastoreutils.api;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Callable;

public interface DataStoreJobCoordinator {

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker);

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, RollbackCallable rollbackWorker);

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, int maxRetries);

    void enqueueJob(AbstractDataStoreJob job) throws InvalidJobException;

    /**
     * This is used by the external applications to enqueue a Job with an
     * appropriate key. A JobEntry is created and queued appropriately.
     */
    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, RollbackCallable rollbackWorker,
            int maxRetries);

    long getClearedTaskCount();

    long getCreatedTaskCount();

    long getIncompleteTaskCount();

    long getPendingTaskCount();
}