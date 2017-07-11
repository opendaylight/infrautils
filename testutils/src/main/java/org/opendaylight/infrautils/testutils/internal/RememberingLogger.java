/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.internal;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * A slf4j logger implementation which remember the last log event.
 *
 * @author Michael Vorburger.ch
 */
public class RememberingLogger extends DelegatingLogger {

    // TODO add the same for warn, info, debug trace ...

    private static volatile String lastErrorMessage;

    public RememberingLogger(Logger delegate) {
        super(delegate);
    }

    public static Optional<String> getLastErrorMessage() {
        return Optional.ofNullable(lastErrorMessage);
    }

    public static void resetLastErrorMessage() {
        lastErrorMessage = null;
    }

    @Override
    public void error(String msg) {
        lastErrorMessage = msg;
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        lastErrorMessage = format;
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        lastErrorMessage = format;
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        lastErrorMessage = format;
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        lastErrorMessage = msg;
        super.error(msg, throwable);
    }

    @Override
    public void error(Marker marker, String msg) {
        lastErrorMessage = msg;
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        lastErrorMessage = format;
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        lastErrorMessage = format;
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        lastErrorMessage = format;
        super.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        lastErrorMessage = msg;
        super.error(marker, msg, throwable);
    }

}
