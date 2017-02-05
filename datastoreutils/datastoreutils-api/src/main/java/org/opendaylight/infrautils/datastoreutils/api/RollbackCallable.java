/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.datastoreutils.api;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class RollbackCallable implements Callable<List<ListenableFuture<Void>>> {

    private volatile List<ListenableFuture<Void>> futures;

    public RollbackCallable() {
    }

    public List<ListenableFuture<Void>> getFutures() {
        return futures;
    }

    public void setFutures(List<ListenableFuture<Void>> futures) {
        this.futures = futures;
    }
}
