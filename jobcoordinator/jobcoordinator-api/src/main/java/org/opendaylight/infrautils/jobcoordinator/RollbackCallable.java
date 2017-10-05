/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.concurrent.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A callable which runs in case a job task fails. It holds the failedFutures that
 * were returned by the last failing job.
 */
public abstract class RollbackCallable implements Callable<List<ListenableFuture<Void>>> {
    @GuardedBy("failedFutures")
    private final List<ListenableFuture<Void>> failedFutures = new ArrayList<>();

    /**
     * Returns the failed failedFutures, i.e. the failedFutures returned by the last failing job, whose work
     * this job is supposed to roll back.
     *
     * @return The failed failedFutures (unmodifiable).
     *
     * @deprecated Use {@link #getFailedFutures()}.
     */
    @Deprecated
    public List<ListenableFuture<Void>> getFutures() {
        return getFailedFutures();
    }

    /**
     * Returns the failed failedFutures, i.e. the failedFutures returned by the last failing job, whose work
     * this job is supposed to roll back.
     *
     * @return The failed failedFutures (unmodifiable).
     */
    public List<ListenableFuture<Void>> getFailedFutures() {
        return Collections.unmodifiableList(failedFutures);
    }

    /**
     * Sets the failed failedFutures, i.e. the failedFutures returned by the last failing job, whose work
     * this job is supposed to roll back.
     *
     * @param futures The failed failedFutures.
     *
     * @deprecated Use {@link #setFailedFutures(List)}.
     */
    public void setFutures(@NonNull List<ListenableFuture<Void>> futures) {
        setFailedFutures(futures);
    }

    /**
     * Sets the failed failedFutures, i.e. the failedFutures returned by the last failing job, whose work
     * this job is supposed to roll back.
     */
    public void setFailedFutures(@NonNull List<ListenableFuture<Void>> futures) {
        synchronized (failedFutures) {
            failedFutures.clear();
            for (ListenableFuture<Void> future : futures) {
                // Prior validation isn't all that helpful, it all boils down to this anyway
                if (future != null) {
                    failedFutures.add(future);
                } else {
                    throw new NullPointerException("setFailedFutures() mustn't be called with a null future");
                }
            }
        }
    }
}
