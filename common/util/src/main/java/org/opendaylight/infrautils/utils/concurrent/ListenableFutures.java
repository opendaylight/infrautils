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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
     * See {@link CompletionStages#toListenableFuture(CompletionStage)} for the inverse function of this.
     * and {@link CompletableFutures#toListenableFuture(java.util.concurrent.CompletableFuture)} for a related function.
     */
    public static <V> CompletionStage<V> toCompletionStage(ListenableFuture<V> future) {
        return CompletionStageWrapper.wrap(ListenableToCompletableFutureWrapper.create(future));
    }

    /**
     * Adds a callback to a ListenableFuture which logs any failures.
     *
     * <p>Instead of using this helper, you should consider directly using
     * {@link Futures#addCallback(ListenableFuture, com.google.common.util.concurrent.FutureCallback,
     * java.util.concurrent.Executor)} to add a callback which does real error recovery in case of a failure instead
     * of just logging an error, if you can.
     *
     * @deprecated Use {@link LoggingFutures#addErrorLogging(ListenableFuture, Logger, String)} instead.
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(ListenableFuture<V> future, Logger logger, String message) {
        LoggingFutures.addErrorLogging(future, logger, message);
    }

    /**
     * Adds a callback to a ListenableFuture which logs any failures.
     *
     * <p>Instead of using this helper, you should consider directly using
     * {@link Futures#addCallback(ListenableFuture, com.google.common.util.concurrent.FutureCallback,
     * java.util.concurrent.Executor)} to add a callback which does real error recovery in case of a failure instead
     * of just logging an error, if you can.
     *
     * @deprecated Use {@link LoggingFutures#addErrorLogging(ListenableFuture, Logger, String, Object)} instead.
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(ListenableFuture<V> future, Logger logger, String format, Object arg) {
        LoggingFutures.addErrorLogging(future, logger, format, arg);
    }

    /**
     * Adds a callback to a ListenableFuture which logs any failures.
     *
     * <p>Instead of using this helper, you should consider directly using
     * {@link Futures#addCallback(ListenableFuture, com.google.common.util.concurrent.FutureCallback,
     * java.util.concurrent.Executor)} to add a callback which does real error recovery in case of a failure instead
     * of just logging an error, if you can.
     *
     * @deprecated Use {@link LoggingFutures#addErrorLogging(ListenableFuture, Logger, String, Object...)} instead.
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(
            ListenableFuture<V> future, Logger logger, String format, Object... arguments) {
        LoggingFutures.addErrorLogging(future, logger, format, arguments);
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
