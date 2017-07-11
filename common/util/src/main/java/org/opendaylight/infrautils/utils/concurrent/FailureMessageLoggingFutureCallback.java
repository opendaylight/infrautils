/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.base.Preconditions;

/**
 * Failure logging future callback with a single String message.
 * @author Michael Vorburger.ch
 */
// package-local not public (for the time being)
final class FailureMessageLoggingFutureCallback<V> extends FailureLoggingFutureCallbackBase<V> {

    private final String message;

    FailureMessageLoggingFutureCallback(String message) {
        super(LOG);
        this.message = Preconditions.checkNotNull(message, "message is null");
    }

    @Override
    public void onFailure(Throwable throwable) {
        logger.error("Future (eventually) failed: " + message, throwable);
    }

}
