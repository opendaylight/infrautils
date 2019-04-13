/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Collections.synchronizedList;
import static org.opendaylight.infrautils.testutils.LogCapture.Level.ERROR;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.infrautils.testutils.LogCapture;
import org.opendaylight.infrautils.testutils.LogRule;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * A slf4j logger implementation which remembers log events. This class is thread-safe.
 *
 * @author Michael Vorburger.ch
 */
public class RememberingLogger extends DelegatingLogger {

    // TODO add the same for warn, info, debug trace ...
    private static final List<LogCapture> ERRORS = synchronizedList(new ArrayList<>());

    RememberingLogger(Logger delegate) {
        super(delegate);
    }

    public static ImmutableList<LogCapture> getErrorLogCaptures() {
        return copyOf(ERRORS);
    }

    public static Optional<String> getLastErrorMessage() {
        return getErrorMessage(0);
    }

    public static Optional<String> getErrorMessage(int howManyMessagesBack) {
        if (ERRORS.size() > howManyMessagesBack) {
            return Optional.ofNullable(ERRORS.get(ERRORS.size() - howManyMessagesBack - 1).getMessage());
        }
        return Optional.empty();
    }

    public static Optional<Throwable> getLastErrorThrowable() {
        return getErrorThrowable(0);
    }

    public static Optional<Throwable> getErrorThrowable(int howManyMessagesBack) {
        if (ERRORS.size() > howManyMessagesBack) {
            return ERRORS.get(ERRORS.size() - howManyMessagesBack - 1).getCause();
        }
        return Optional.empty();
    }

    public static void resetLastError() {
        ERRORS.clear();
    }

    @Override
    public void error(String msg) {
        ERRORS.add(new LogCapture(ERROR, msg, null));
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTuple mf = MessageFormatter.format(format, arg);
        ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
        ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
        ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable throwable) {
        ERRORS.add(new LogCapture(ERROR, msg, throwable));
        super.error(msg, throwable);
    }

    @Override
    public void error(Marker marker, String msg) {
        if (!LogRule.getMarker().equals(marker)) {
            ERRORS.add(new LogCapture(ERROR, msg, null));
        }
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg);
            ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.format(format, arg1, arg2);
            ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (!LogRule.getMarker().equals(marker)) {
            FormattingTuple mf = MessageFormatter.arrayFormat(format, arguments);
            ERRORS.add(new LogCapture(ERROR, mf.getMessage(), mf.getThrowable()));
        }
        super.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        if (!LogRule.getMarker().equals(marker)) {
            ERRORS.add(new LogCapture(ERROR, msg, throwable));
        }
        super.error(marker, msg, throwable);
    }
}
