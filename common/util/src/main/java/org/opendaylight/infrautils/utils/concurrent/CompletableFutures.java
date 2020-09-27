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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Utilities for {@link CompletableFuture}.
 *
 * @author Michael Vorburger.ch
 * @deprecated Use <a href="https://github.com/lukas-krecan/future-converter">Future Converter</a> instead.
 */
@Deprecated(forRemoval = true)
public final class CompletableFutures {
    private CompletableFutures() {

    }

    /**
     * Converts a Java 8 CompletionStage to a Guava ListenableFuture.
     * See {@link CompletionStages#toListenableFuture(CompletionStage)} for a related function.
     */
    public static <V> ListenableFuture<V> toListenableFuture(CompletableFuture<V> completableFuture) {
        return new CompletableToListenableFutureWrapper<>(completableFuture);
    }

    /**
     * Return an immediately exceptional completed CompletableFuture.
     *
     * <p>Inspired by Guava's {@link Futures#immediateFailedFuture(Throwable)}.
     *
     * <p>
     * Useful because while CompletableFuture does have a
     * {@link CompletableFuture#completedFuture(Object)} static factory method
     * to obtain immediate non-exceptional value completion, it is missing a
     * static factory method like this. This is often useful, e.g. when catching
     * exceptions to transform into CompletableFuture to return, and saves the 3
     * lines to create a new instance, invoke the non-static
     * {@link CompletableFuture#completeExceptionally(Throwable)} and return the
     * completableFuture. (It's not possible to use chained invocation style,
     * because completeExceptionally does not return this but a boolean, which
     * is useless when requiring an immediately completed CompletionStage.)
     */
    public static <T> CompletableFuture<T> completedExceptionally(Throwable throwable) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(throwable);
        return completableFuture;
    }
}
