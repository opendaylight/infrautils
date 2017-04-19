/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;

/**
 * Additional factory and utility methods for executors.
 */
public class MoreExecutors {

    private MoreExecutors() {
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
    public static ExecutorService newSingleThreadExecutor(final String namePrefix, final Logger logger) {
        return Executors.newSingleThreadExecutor(
                ThreadFactoryProvider.builder()
                        .namePrefix(namePrefix)
                        .logger(logger)
                        .build()
                        .get());
    }
}
