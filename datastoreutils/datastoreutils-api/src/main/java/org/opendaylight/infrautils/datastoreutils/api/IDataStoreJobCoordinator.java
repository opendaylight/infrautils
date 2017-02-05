package org.opendaylight.infrautils.datastoreutils.api;

import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListenableFuture;

public interface IDataStoreJobCoordinator {

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker);

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, RollbackCallable rollbackWorker);

    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, int maxRetries);

    void enqueueJob(AbstractDataStoreJob job) throws InvalidJobException;

    /**
     *    This is used by the external applications to enqueue a Job
     *    with an appropriate key. A JobEntry is created and queued
     *    appropriately.
     */
    void enqueueJob(String key, Callable<List<ListenableFuture<Void>>> mainWorker, RollbackCallable rollbackWorker,
            int maxRetries);

    long getIncompleteTaskCount();

}