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
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * A slf4j logger implementation which remember the last log event.
 *
 * @author Michael Vorburger.ch
 */
public class RememberingLogger extends DelegatingLogger {

    // TODO add the same for warn, info, debug trace ...

    private static volatile String lastErrorMessage;
    private static volatile Throwable lastErrorThrowable;

    RememberingLogger(Logger delegate) {
        super(delegate);
    }

    public static Optional<String> getLastErrorMessage() {
        return Optional.ofNullable(lastErrorMessage);
    }

    public static Optional<Throwable> getLastErrorThrowable() {
        return Optional.ofNullable(lastErrorThrowable);
    }

    public static void resetLastError() {
        lastErrorMessage = null;
        lastErrorThrowable = null;
    }

    @Override
    public void error(String msg) {
        lastErrorMessage = msg;
        lastErrorThrowable = null;
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTuple mf = MessageFormatter.format(format, arg);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        lastErrorMessage = msg;
        lastErrorThrowable = throwable;
        super.error(msg, throwable);
    }

    @Override
    public void error(Marker marker, String msg) {
        lastErrorMessage = msg;
        lastErrorThrowable = null;
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        FormattingTuple mf = MessageFormatter.format(format, arg);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
        lastErrorMessage = mf.getMessage();
        lastErrorThrowable = mf.getThrowable();
        super.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        lastErrorMessage = msg;
        lastErrorThrowable = throwable;
        super.error(marker, msg, throwable);
    }

}
