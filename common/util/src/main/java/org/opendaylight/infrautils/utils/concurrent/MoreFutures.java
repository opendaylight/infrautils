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

/**
 * More static utility methods pertaining to the Future interface.
 *
 * @see Futures
 *
 * @author Michael Vorburger.ch
 */
public class MoreFutures {

    /**
     * Adds a callback to a Future which logs any failures.
     *
     * <p>
     * Instead of using this helper, you should consider directly using
     * {@link Futures#addCallback(ListenableFuture, com.google.common.util.concurrent.FutureCallback,
     * java.util.concurrent.Executor)} to add a callback which does real error recovery in case of a failure instead
     * of just logging an error, if you can.
     */
    public static <V> void logFailure(ListenableFuture<V> future, String message) {
        Futures.addCallback(future, new FailureLoggingFutureCallback<V>(message), MoreExecutors.directExecutor());
    }
}
