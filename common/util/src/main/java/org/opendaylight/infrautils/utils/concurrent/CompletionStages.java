/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Utilities for {@link CompletionStage}.
 *
 * @author Michael Vorburger.ch
 * @deprecated Use <a href="https://github.com/lukas-krecan/future-converter">Future Converter</a> instead.
 */
@Deprecated(forRemoval = true)
public final class CompletionStages {
    private CompletionStages() {

    }

    /**
     * Converts a Java 8 CompletionStage to a Guava ListenableFuture.
     * See {@link ListenableFutures#toCompletionStage(ListenableFuture)} for the exact inverse function of this,
     * and {@link CompletableFutures#toListenableFuture(java.util.concurrent.CompletableFuture)} for a related function.
     */
    public static <V> ListenableFuture<V> toListenableFuture(CompletionStage<V> completionStage) {
        return CompletableFutures.toListenableFuture(completionStage.toCompletableFuture());
    }

    /**
     * Return an immediately exceptional completed CompletionStage.
     * See {@link CompletableFutures#completedExceptionally(Throwable)}.
     */
    public static <T> CompletionStage<T> completedExceptionally(Throwable throwable) {
        return CompletionStageWrapper.wrap(CompletableFutures.completedExceptionally(throwable));
    }
}
