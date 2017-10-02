/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.CompletableFuture;

// package local, not public
final class ListenableToCompletableFutureWrapper<V> extends CompletableFuture<V> implements FutureCallback<V> {

    // This implementation is inspired by a spotify/futures-extra's class of the same name
    // remixed with https://blog.krecan.net/2014/06/11/converting-listenablefutures-to-completablefutures-and-back/

    private final ListenableFuture<V> guavaListenableFuture;

    ListenableToCompletableFutureWrapper(final ListenableFuture<V> guavaListenableFuture) {
        this.guavaListenableFuture = checkNotNull(guavaListenableFuture, "guavaListenableFuture");
        Futures.addCallback(guavaListenableFuture, this, MoreExecutors.directExecutor());
    }

    @Override
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE") // https://github.com/findbugsproject/findbugs/issues/79
    public void onSuccess(final V result) {
        complete(result);
    }

    @Override
    public void onFailure(final Throwable throwable) {
        completeExceptionally(throwable);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = guavaListenableFuture.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
    }
}
