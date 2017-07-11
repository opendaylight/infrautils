/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import junit.framework.AssertionFailedError;
import org.junit.ComparisonFailure;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opendaylight.infrautils.testutils.internal.RememberingLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Rule which "captures" slf4j-simple logs. By default, it fails the test
 * if an error was logged. It can also pass the test if a logged error was
 * expected (but fail it otherwise), and dito for other log levels.
 *
 * <p>Usage:
 *
 * <pre>
 *   public {@literal @}Rule LogCaptureRule logCaptureRule = new LogCaptureRule();
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
 * which we already widely use in ODL).
 *
 * @see ExpectedException
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRule implements TestRule {

    private String expectedErrorLogMessage;

    public LogCaptureRule() {
        classpathTest();
    }

    private void classpathTest() {
        Logger log = LoggerFactory.getLogger(LogCaptureRule.class);
        if (!(log instanceof RememberingLogger)) {
            throw new IllegalStateException("infrautils-testutils must be on classpath BEFORE slf4j-simple!");
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {

            @Override
            @SuppressWarnings("checkstyle:IllegalCatch")
            public void evaluate() throws Throwable {
                RememberingLogger.resetLastErrorMessage();
                statement.evaluate();
                RememberingLogger.getLastErrorMessage().ifPresent(lastErrorLogMessage -> {
                    if (expectedErrorLogMessage == null) {
                        throw new AssertionFailedError(
                                "LogCaptureRule expected no error log, but: " + lastErrorLogMessage);
                    } else if (!expectedErrorLogMessage.equals(lastErrorLogMessage)) {
                        throw new ComparisonFailure("LogCaptureRule expected different error message",
                                expectedErrorLogMessage, lastErrorLogMessage);
                    }
                });
                if (!RememberingLogger.getLastErrorMessage().isPresent() && expectedErrorLogMessage != null) {
                    throw new AssertionFailedError("LogCaptureRule expected an error log: " + expectedErrorLogMessage);
                }
            }
        };
    }

    public void expectError(String message) {
        this.expectedErrorLogMessage = message;
    }
}
