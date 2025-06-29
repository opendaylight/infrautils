/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import org.immutables.value.Value;
import org.slf4j.Logger;

/**
 * Builder for {@link ThreadFactory}. Easier to use than other alternatives because it enforces setting all required
 * properties through a staged builder.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(stagedBuilder = true)
public abstract class ThreadFactoryProvider {

    public static ImmutableThreadFactoryProvider.NamePrefixBuildStage builder() {
        return ImmutableThreadFactoryProvider.builder();
    }

    /**
     * Prefix for threads from this factory. For example, "rpc-pool", to create
     * "rpc-pool-1/2/3" named threads. Note that this is a prefix, not a format,
     * so you pass just "rpc-pool" instead of e.g. "rpc-pool-%d".
     */
    @Value.Parameter public abstract String namePrefix();

    /**
     * Logger used to log uncaught exceptions from new threads created via this factory.
     */
    @Value.Parameter public abstract Logger logger();

    /**
     * Priority for new threads from this factory.
     */
    @Value.Parameter public abstract Optional<Integer> priority();

    /**
     * Daemon or not for new threads created via this factory.
     * <b>NB: Defaults to true.</b>
     */
    @Value.Default public boolean daemon() {
        return true;
    }

    public ThreadFactory get() {
        var prefix = namePrefix();
        var builder = Thread.ofPlatform()
            .name(prefix + '-', 0)
            .daemon(true)
            .uncaughtExceptionHandler(LoggingThreadUncaughtExceptionHandler.toLogger(logger()));
        priority().ifPresent(builder::priority);
        logger().info("ThreadFactory for {} created", prefix);
        return builder.factory();
    }
}
