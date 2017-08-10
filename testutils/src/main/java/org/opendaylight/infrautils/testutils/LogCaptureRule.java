/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import junit.framework.AssertionFailedError;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.junit.ComparisonFailure;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule which "captures" logs. By default, it fails the test
 * if an error was logged. It can also pass the test if a logged error was
 * expected (but fail it otherwise).
 *
 * <p>Usage:
 *
 * <pre>
 *   public static {@literal @}ClassRule LogCaptureRule logCaptureRule = new LogCaptureRule();
 *
 *   {@literal @}Test ...
 *
 *       logRule.expectError();
 * </pre>
 *
 * <p>See also e.g.
 * <a href="http://projects.lidalia.org.uk/slf4j-test/">slf4j-test</a> or
 * <a href="https://github.com/portingle/slf4jtesting">slf4jtesting</a>
 * (both of which, contrary to this, don't integrate with slf4j-simple
 * which we already widely use in ODL tests).
 *
 * @see ExpectedException
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRule implements TestRule {

    // We intentionally delegate to instead of extend LoggerContextRule, to make its use a hidden implementation detail
    private final LoggerContextRule log4jLoggerContextRule;

    private String expectedErrorLogMsg;

    public LogCaptureRule() {
        log4jLoggerContextRule = new LoggerContextRule();
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return log4jLoggerContextRule.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();

                    final Optional<LogEvent> lastErrorLog = getLastErrorLog();
                    lastErrorLog.ifPresent(lastErrorLogEvent -> {
                        String lastErrorLogMsg = lastErrorLogEvent.getMessage().getFormattedMessage();
                        if (expectedErrorLogMsg == null) {
                            throw new AssertionFailedError(
                                    "LogCaptureRule expected no error log, but: " + lastErrorLogMsg);
                        } else if (!expectedErrorLogMsg.equals(lastErrorLogMsg)) {
                            throw new ComparisonFailure("LogCaptureRule expected different error message",
                                    expectedErrorLogMsg, lastErrorLogMsg);
                        }
                    }); // else
                    if (!lastErrorLog.isPresent() && expectedErrorLogMsg != null) {
                        throw new AssertionFailedError("LogCaptureRule expected an error log: " + expectedErrorLogMsg);
                    }
                } finally {
                    // We HAVE to clean-up after each @Test, because this is (has to be) used as a static @ClassRule ...
                    log4jLoggerContextRule.getListAppender("List").clear();
                    expectedErrorLogMsg = null;
                }
            }
        }, description);
    }

    public void expectError(String message) {
        this.expectedErrorLogMsg = message;
    }

    public Throwable getLastErrorThrowable() {
        return getLastErrorLog().get().getThrown();
    }

    private Optional<LogEvent> getLastErrorLog() {
        // If this fails with an NPE in getConfiguration(), then it was called outside of log4jLoggerContextRule.apply()
        final List<LogEvent> events = log4jLoggerContextRule.getListAppender("List").getEvents();
        final Stream<LogEvent> errorEvents = events.stream()
                .filter(logEvent -> logEvent.getLevel().equals(Level.ERROR));
        // The reduce() is really a findLast(), see https://stackoverflow.com/a/21441634/421602
        final Optional<LogEvent> last = errorEvents.reduce((first, second) -> second);
        return last;
    }
}
