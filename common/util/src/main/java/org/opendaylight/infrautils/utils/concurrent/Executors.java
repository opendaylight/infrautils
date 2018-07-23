/*
 * Copyright (c) 2017, 2018 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * Additional factory and utility methods for executors.
 *
 * <p>Use this instead of {@link java.util.concurrent.Executors}, because
 * it ensures that the returned Executor uses a {@link ThreadFactory} that is
 * named, has a logging UncaughtExceptionHandler, and returns (Guava's) ListenableFuture.
 */
public final class Executors {

    public static final long DEFAULT_TIMEOUT_FOR_SHUTDOWN = 10;
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT_FOR_SHUTDOWN = TimeUnit.SECONDS;

    private Executors() {
    }

    /**
     * Creates a single thread executor with a {@link ThreadFactory} that uses
     * the provided prefix for its thread names and logs uncaught exceptions
     * with the specified {@link Logger}.
     *
     * @param namePrefix Prefix for this executor thread names
     * @param logger Logger used to log uncaught exceptions
     * @return the newly created single-threaded Executor
     */
    public static ListeningExecutorService newListeningSingleThreadExecutor(String namePrefix, Logger logger) {
        return MoreExecutors.listeningDecorator(newSingleThreadExecutor(namePrefix, logger));
    }

    /**
     * Deprecated single thread executor.
     * @deprecated Use {@link #newListeningSingleThreadExecutor(String, Logger)} instead.
     */
    @Deprecated
    public static ExecutorService newSingleThreadExecutor(String namePrefix, Logger logger) {
        return java.util.concurrent.Executors.newSingleThreadExecutor(
                ThreadFactoryProvider.builder()
                        .namePrefix(namePrefix)
                        .logger(logger)
                        .build()
                        .get());
    }

    public static ListeningExecutorService newListeningCachedThreadPool(String namePrefix, Logger logger) {
        return MoreExecutors.listeningDecorator(newCachedThreadPool(namePrefix, logger));
    }

    @Deprecated
    public static ExecutorService newCachedThreadPool(String namePrefix, Logger logger) {
        return java.util.concurrent.Executors.newCachedThreadPool(
                ThreadFactoryProvider.builder()
                        .namePrefix(namePrefix)
                        .logger(logger)
                        .build()
                        .get());
    }

    public static ListeningScheduledExecutorService newListeningSingleThreadScheduledExecutor(String namePrefix,
            Logger logger) {
        return MoreExecutors.listeningDecorator(newSingleThreadScheduledExecutor(namePrefix, logger));
    }

    @Deprecated
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String namePrefix, Logger logger) {
        return java.util.concurrent.Executors.unconfigurableScheduledExecutorService(
                   java.util.concurrent.Executors.newSingleThreadScheduledExecutor(
                    ThreadFactoryProvider.builder()
                            .namePrefix(namePrefix)
                            .logger(logger)
                            .build()
                            .get()));
    }

    public static ListeningScheduledExecutorService newListeningScheduledThreadPool(int corePoolSize, String namePrefix,
            Logger logger) {
        return MoreExecutors.listeningDecorator(newScheduledThreadPool(corePoolSize, namePrefix, logger));
    }

    @Deprecated
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix,
            Logger logger) {
        return java.util.concurrent.Executors.newScheduledThreadPool(corePoolSize,
                ThreadFactoryProvider.builder()
                        .namePrefix(namePrefix)
                        .logger(logger)
                        .build()
                        .get());
    }

    public static void shutdownAndAwaitTermination(ExecutorService executorService) {
        MoreExecutors.shutdownAndAwaitTermination(executorService, DEFAULT_TIMEOUT_FOR_SHUTDOWN,
                                                  DEFAULT_TIMEOUT_UNIT_FOR_SHUTDOWN);
    }
}
