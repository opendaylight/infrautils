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
 * <p>See also Guava's {@link JdkFutureAdapters} utility, which is used internally here
 * (with an Executor that would log any possible issues, and which will be metric monitored).
 *
 * @author Michael Vorburger.ch
 */
public final class JdkFutures {

    private static final Logger LOG = LoggerFactory.getLogger(JdkFutures.class);

    private static final Executor EXECUTOR = Executors.newCachedThreadPool("JdkFutures", LOG);

    private JdkFutures() { }

    /**
     * Converts a Java 5 {@link Future} to a Guava {@link ListenableFuture}.
     */
    public static <V> ListenableFuture<V> toListenableFuture(Future<V> future) {
        return JdkFutureAdapters.listenInPoolThread(future, EXECUTOR);
    }

    /**
     * Converts a Java 5 {@link Future} to a Java 8 {@link CompletionStage}.
     */
    public static <V> CompletionStage<V> toCompletionStage(Future<V> future) {
        return ListenableFutures.toCompletionStage(toListenableFuture(future));
    }

    /**
     * Adds a callback to a Future which logs any failures.
     * @see ListenableFutures#addErrorLogging(ListenableFuture, Logger, String)
     */
    public static <V> void addErrorLogging(Future<V> future, Logger logger, String message) {
        ListenableFutures.addErrorLogging(toListenableFuture(future), logger, message);
    }

    public static <V> void addErrorLogging(Future<V> future, Logger logger, String format, Object arg) {
        ListenableFutures.addErrorLogging(toListenableFuture(future), logger, format, arg);
    }

    public static <V> void addErrorLogging(Future<V> future, Logger logger, String format, Object... args) {
        ListenableFutures.addErrorLogging(toListenableFuture(future), logger, format, args);
    }

}
