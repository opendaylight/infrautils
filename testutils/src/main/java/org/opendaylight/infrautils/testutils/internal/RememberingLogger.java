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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
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

    // TODO add the same for warn, info, debug trace ...

    @GuardedBy("this")
    private static final List<String> ERROR_MESSAGES = synchronizedList(new ArrayList<>());

    @GuardedBy("this")
    private static final List<Throwable> ERROR_THROWABLES = synchronizedList(new ArrayList<>());

    RememberingLogger(Logger delegate) {
        super(delegate);
    }

    public static Optional<String> getLastErrorMessage() {
        return getErrorMessage(0);
    }

    public static synchronized Optional<String> getErrorMessage(int howManyMessagesBack) {
        if (ERROR_MESSAGES.size() > howManyMessagesBack) {
            return Optional.ofNullable(ERROR_MESSAGES.get(ERROR_MESSAGES.size() - howManyMessagesBack - 1));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Throwable> getLastErrorThrowable() {
        return getErrorThrowable(0);
    }

    public static Optional<Throwable> getErrorThrowable(int howManyMessagesBack) {
        if (ERROR_THROWABLES.size() > howManyMessagesBack) {
            return Optional.ofNullable(ERROR_THROWABLES.get(ERROR_THROWABLES.size() - howManyMessagesBack - 1));
        } else {
            return Optional.empty();
        }
    }

    public static void resetLastError() {
        ERROR_MESSAGES.clear();
        ERROR_THROWABLES.clear();
    }

    @Override
    public void error(String msg) {
        synchronized (this) {
            ERROR_MESSAGES.add(msg);
            ERROR_THROWABLES.add(null);
        }
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTuple mf = MessageFormatter.format(format, arg);
        synchronized (this) {
            ERROR_MESSAGES.add(mf.getMessage());
            ERROR_THROWABLES.add(mf.getThrowable());
        }
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
        synchronized (this) {
            ERROR_MESSAGES.add(mf.getMessage());
            ERROR_THROWABLES.add(mf.getThrowable());
        }
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
        synchronized (this) {
            ERROR_MESSAGES.add(mf.getMessage());
            ERROR_THROWABLES.add(mf.getThrowable());
        }
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        synchronized (this) {
            ERROR_MESSAGES.add(msg);
            ERROR_THROWABLES.add(throwable);
        }
        super.error(msg, throwable);
    }

    @Override
    public void error(Marker marker, String msg) {
        if (!LogRule.getMarker().equals(marker)) {
            synchronized (this) {
                ERROR_MESSAGES.add(msg);
                ERROR_THROWABLES.add(null);
            }
        }
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg);
            synchronized (this) {
                ERROR_MESSAGES.add(mf.getMessage());
                ERROR_THROWABLES.add(mf.getThrowable());
            }
        }
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
            synchronized (this) {
                ERROR_MESSAGES.add(mf.getMessage());
                ERROR_THROWABLES.add(mf.getThrowable());
            }
        }
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
            synchronized (this) {
                ERROR_MESSAGES.add(mf.getMessage());
                ERROR_THROWABLES.add(mf.getThrowable());
            }
        }
        super.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        if (!LogRule.getMarker().equals(marker)) {
            synchronized (this) {
                ERROR_MESSAGES.add(msg);
                ERROR_THROWABLES.add(throwable);
            }
        }
        super.error(marker, msg, throwable);
    }

}
