/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.internal;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.testutils.LogRule;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * A slf4j logger implementation which remember the last log event.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public class RememberingLogger extends DelegatingLogger {

    private static class LogMessageAndCause {
        private final String message;
        private final @Nullable Throwable cause;

        LogMessageAndCause(String message, @Nullable Throwable cause) {
            this.message = message;
            this.cause = cause;
        }
    }

    // TODO add the same for warn, info, debug trace ...

    private static final List<LogMessageAndCause> ERRORS = synchronizedList(new ArrayList<>());

    RememberingLogger(Logger delegate) {
        super(delegate);
    }

    public static Optional<String> getLastErrorMessage() {
        return getErrorMessage(0);
    }

    public static Optional<String> getErrorMessage(int howManyMessagesBack) {
        if (ERRORS.size() > howManyMessagesBack) {
            return Optional.ofNullable(ERRORS.get(ERRORS.size() - howManyMessagesBack - 1).message);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Throwable> getLastErrorThrowable() {
        return getErrorThrowable(0);
    }

    public static Optional<Throwable> getErrorThrowable(int howManyMessagesBack) {
        if (ERRORS.size() > howManyMessagesBack) {
            return Optional.ofNullable(ERRORS.get(ERRORS.size() - howManyMessagesBack - 1).cause);
        } else {
            return Optional.empty();
        }
    }

    public static void resetLastError() {
        ERRORS.clear();
    }

    @Override
    public void error(String msg) {
        ERRORS.add(new LogMessageAndCause(msg, null));
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTuple mf = MessageFormatter.format(format, arg);
        ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
        ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
        ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        ERRORS.add(new LogMessageAndCause(msg, throwable));
        super.error(msg, throwable);
    }

    @Override
    public void error(Marker marker, String msg) {
        if (!LogRule.getMarker().equals(marker)) {
            ERRORS.add(new LogMessageAndCause(msg, null));
        }
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg);
            ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
            ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
            ERRORS.add(new LogMessageAndCause(mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        if (!LogRule.getMarker().equals(marker)) {
            ERRORS.add(new LogMessageAndCause(msg, throwable));
        }
        super.error(marker, msg, throwable);
    }

}
