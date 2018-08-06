/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods pertaining to the Java 5 {@link Future} interface.
 *
 * <p>Use of JDK Future should generally be avoid, but is prevalent in ODL because
 * the Java Bindings (v1) generated code uses them for all RPC return types.
 *
 * <p>Any new hand written code should never use the old Future interface anymore,
 * but instead use either a Guava {@link ListenableFuture}, or a Java 8 {@link CompletionStage}.
 *
 * @author Michael Vorburger.ch
 */
public final class JdkFutures {

    private static final Logger LOG = LoggerFactory.getLogger(JdkFutures.class);

    private static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool("JdkFutures", LOG);

    private JdkFutures() { }

    /**
     * Converts a Java 5 {@link Future} to a Guava {@link ListenableFuture}.
     *
     * <p>See also Guava's {@link JdkFutureAdapters} utility, which is used internally here
     *     (with an Executor that would log any possible issues, and which will be metric monitored).
     */
    public static <V> ListenableFuture<V> toListenableFuture(Future<V> future) {
        return JdkFutureAdapters.listenInPoolThread(future, DEFAULT_EXECUTOR);
    }

    /**
     * Converts a Java 5 {@link Future} to a Guava {@link ListenableFuture}, using your own Executor.
     *
     * <p>It's typically a bad idea to pass a directExector here, because then the
     * {@link ListenableFuture#addListener(Runnable, Executor)} will become blocking
     * and one might as well just use the Future's blocking get() instead of this.
     */
    public static <V> ListenableFuture<V> toListenableFuture(Future<V> future, Executor executor) {
        return JdkFutureAdapters.listenInPoolThread(future, executor);
    }

    /**
     * Converts a Java 5 {@link Future} to a Java 8 {@link CompletionStage}.
     */
    @SuppressWarnings("unchecked")
    public static <V> CompletionStage<V> toCompletionStage(Future<V> future) {
        // typical CompletionStage implementation is java.util.concurrent.CompletableFuture
        // which is both a Future and a CompletionStage, so we can save a spin wait and avoid
        // the intermediate ListenableFuture which we have to use otherwise:
        if (future instanceof CompletionStage) {
            return (CompletionStage<V>) future;
        }
        return ListenableFutures.toCompletionStage(toListenableFuture(future));
    }

    /**
     * Adds a callback to a Future which logs any failures.
     *
     * @see LoggingFutures#addErrorLogging(ListenableFuture, Logger, String)
     * @deprecated Use {@link LoggingFutures#addErrorLogging(Future, Logger, String)}
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(Future<V> future, Logger logger, String message) {
        LoggingFutures.addErrorLogging(future, logger, message);
    }

    /**
     * Adds a callback to a Future which logs any failures.
     *
     * @see LoggingFutures#addErrorLogging(ListenableFuture, Logger, String, Object)
     * @deprecated Use {@link LoggingFutures#addErrorLogging(Future, Logger, String, Object)}
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(Future<V> future, Logger logger, String format, Object arg) {
        LoggingFutures.addErrorLogging(future, logger, format, arg);
    }

    /**
     * Adds a callback to a Future which logs any failures.
     *
     * @see LoggingFutures#addErrorLogging(ListenableFuture, Logger, String, Object...)
     * @deprecated Use {@link LoggingFutures#addErrorLogging(Future, Logger, String, Object...)}
     */
    @Deprecated
    @SuppressWarnings("FutureReturnValueIgnored")
    public static <V> void addErrorLogging(Future<V> future, Logger logger, String format, Object... args) {
        LoggingFutures.addErrorLogging(future, logger, format, args);
    }
}
