/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Consumer;
import org.eclipse.jdt.annotation.Nullable;
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
 * expected (but fail it otherwise).
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
 * which we already widely use in ODL tests).
 *
 * @see ExpectedException
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRule implements TestRule {

    private @Nullable Consumer<ImmutableList<LogCapture>> errorLogHandler;

    @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "TYPE_USE and SpotBugs")
    public LogCaptureRule() {
        classpathTest();
    }

    private static void classpathTest() {
        Logger log = LoggerFactory.getLogger(LogCaptureRule.class);
        checkState(log instanceof RememberingLogger, "infrautils-testutils must be on classpath BEFORE slf4j-simple!");
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {

            @Override
            @SuppressWarnings("checkstyle:IllegalCatch")
            public void evaluate() throws Throwable {
                RememberingLogger.resetLastError();
                @Var @Nullable Throwable testFailingThrowable = null;
                try {
                    statement.evaluate();
                } catch (Throwable t) {
                    testFailingThrowable = t;
                }

                try {
                    if (errorLogHandler != null) {
                        errorLogHandler.accept(RememberingLogger.getErrorLogCaptures());
                    } else {
                        RememberingLogger.getLastErrorMessage().ifPresent(lastErrorLogMessage -> {
                            throw new LogCaptureRuleException(
                                "Expected no error log, but: " + lastErrorLogMessage,
                                    RememberingLogger.getLastErrorThrowable().orElse(null));
                        });
                    }
                } catch (RuntimeException e) {
                    if (testFailingThrowable != null) {
                        e.addSuppressed(testFailingThrowable);
                    }
                    throw e;
                }

                if (testFailingThrowable != null) {
                    throw testFailingThrowable;
                }
            }
        };
    }

    public void handleErrorLogs(Consumer<ImmutableList<LogCapture>> newErrorLogHandler) {
        checkState(this.errorLogHandler == null, "errorLogHandler already set, can only set once per @Test method");
        this.errorLogHandler = requireNonNull(newErrorLogHandler, "newErrorLogHandler");
    }

    public void expectLastErrorMessageContains(String partialErrorMessage) {
        requireNonNull(partialErrorMessage, "partialErrorMessage");
        handleErrorLogs(logCaptures -> {
            String errorMessage = RememberingLogger.getLastErrorMessage().orElseThrow(
                () -> new LogCaptureRuleException("Expected error log message to contain: " + partialErrorMessage,
                        null));
            if (!errorMessage.contains(partialErrorMessage)) {
                throw new AssertionError("LogCaptureRule expected error message containing: "
                        + partialErrorMessage + " but was: " + errorMessage);
            }
        });
    }

    public void expectError(String message, int howManyMessagesBack) {
        requireNonNull(message, "message");
        handleErrorLogs(logCaptures -> {
            String errorMessage = RememberingLogger.getErrorMessage(howManyMessagesBack).orElseThrow(
                () -> new LogCaptureRuleException("Expected error log message: " + message, null));
            if (!errorMessage.equals(message)) {
                throw new ComparisonFailure("LogCaptureRule expected different error message",
                        message, errorMessage);
            }
        });
    }

    public void expectError(String message) {
        expectError(message, 0);
    }

    public Throwable getLastErrorThrowable() {
        return RememberingLogger.getLastErrorThrowable().get();
    }

    public Throwable getErrorThrowable(int howManyMessagesBack) {
        return RememberingLogger.getErrorThrowable(howManyMessagesBack).get();
    }
}
