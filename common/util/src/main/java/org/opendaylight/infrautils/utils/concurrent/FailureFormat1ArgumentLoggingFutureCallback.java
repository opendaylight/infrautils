/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.base.Preconditions;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;

/**
 * Failure logging future callback with a message format String and 1 argument.
 * @author Michael Vorburger.ch
 */
// package-local not public (for the time being)
// TODO Have to re-specify <@NonNull V> here seems wrong; see https://github.com/lastnpe/eclipse-null-eea-augments/pull/44/files
final class FailureFormat1ArgumentLoggingFutureCallback<@NonNull V> extends FailureLoggingFutureCallbackBase<V> {

    private final String format;
    private final Object arg;

    FailureFormat1ArgumentLoggingFutureCallback(Logger logger, String format, Object arg) {
        super(logger);
        this.format = Preconditions.checkNotNull(format, "format is null");
        this.arg = arg; // do *NOT* null check this one (that's valid)
    }

    @Override
    public void onFailure(Throwable throwable) {
        logger.error("Future (eventually) failed: " + format, arg, throwable);
    }

}
