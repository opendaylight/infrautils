/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.CompletableFuture;

/**
 * Utilities for {@link CompletableFuture}.
 *
 * @author Michael Vorburger.ch
 */
public final class CompletableFutures {

    private CompletableFutures() { }

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
     * is useless when requiring and immediately completed CompletionStage.)
     */
    public static CompletableFuture<?> completedExceptionally(Throwable throwable) {
        CompletableFuture<?> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(throwable);
        return completableFuture;
    }

}
