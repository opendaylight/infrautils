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
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;

/**
 * More static utility methods pertaining to Guava's ListenableFuture interface.
 *
 * @see Futures
 *
 * @author Michael Vorburger.ch
 */
public final class ListenableFutures {

    private ListenableFutures() { }

    /**
     * Converts a Guava ListenableFuture to a Java 8 CompletionStage.
     * Callers should not cast the returned CompletionStage by this method to CompletableFuture (as it may not be one).
     */
    public static <V> CompletionStage<V> toCompletionStage(ListenableFuture<V> future) {
        return CompletionStageWrapper.wrap(new ListenableToCompletableFutureWrapper<>(future));
    }

    /**
     * Adds a callback to a ListenableFuture which logs any failures.
     *
     * <p>Instead of using this helper, you should consider directly using
     * {@link Futures#addCallback(ListenableFuture, com.google.common.util.concurrent.FutureCallback,
     * java.util.concurrent.Executor)} to add a callback which does real error recovery in case of a failure instead
     * of just logging an error, if you can.
     */
    public static <V> void addErrorLogging(ListenableFuture<V> future, Logger logger, String message) {
        Futures.addCallback(future, new FailureMessageLoggingFutureCallback<>(logger, message),
                MoreExecutors.directExecutor());
    }

    public static <V> void addErrorLogging(ListenableFuture<V> future, Logger logger, String format, Object arg) {
        Futures.addCallback(future,
                new FailureFormat1ArgumentLoggingFutureCallback<V>(logger, format, arg),
                MoreExecutors.directExecutor());
    }

    public static <V> void addErrorLogging(
            ListenableFuture<V> future, Logger logger, String format, Object... arguments) {
        Futures.addCallback(future,
                new FailureFormatMoreArgumentsLoggingFutureCallback<V>(logger, format, arguments),
                MoreExecutors.directExecutor());
    }
}
