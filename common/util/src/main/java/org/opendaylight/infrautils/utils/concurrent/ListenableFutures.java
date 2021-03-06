/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * More static utility methods pertaining to Guava's ListenableFuture interface.
 *
 * @see Futures
 *
 * @author Michael Vorburger.ch
 */
public final class ListenableFutures {
    private ListenableFutures() {

    }

    public static <V, E extends Exception> V checkedGet(ListenableFuture<V> future,
            Function<? super Exception, E> mapper) throws E {
        try {
            return future.get();
        // as in com.google.common.util.concurrent.AbstractCheckedFuture.checkedGet:
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw mapper.apply(e);
        } catch (CancellationException e) {
            throw mapper.apply(e);
        } catch (ExecutionException e) {
            throw mapper.apply(e);
        }
    }

    public static <V, E extends Exception> V checkedGet(ListenableFuture<V> future,
            Function<? super Exception, E> mapper, long timeout, TimeUnit unit) throws E, TimeoutException {
        try {
            return future.get(timeout, unit);
        // as in com.google.common.util.concurrent.AbstractCheckedFuture.checkedGet:
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw mapper.apply(e);
        } catch (CancellationException e) {
            throw mapper.apply(e);
        } catch (ExecutionException e) {
            throw mapper.apply(e);
        }
    }
}
