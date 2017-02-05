/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A callable which runs in case a job task fails. It holds the futures that
 * were returned by the last failing job.
 */
public abstract class RollbackCallable implements Callable<List<ListenableFuture<Void>>> {

    private volatile List<ListenableFuture<Void>> futures;

    public List<ListenableFuture<Void>> getFutures() {
        return futures;
    }

    /**
     * Sets the failed futures which resulted in this RollbackCallable being
     * called.
     */
    public void setFutures(@NonNull List<ListenableFuture<Void>> futures) {
        this.futures = futures;
    }
}
