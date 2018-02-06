/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Var;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
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

    public LogCaptureRule() {
        classpathTest();
    }

    private static void classpathTest() {
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
        this.errorLogHandler = newErrorLogHandler;
    }

    public void expectLastErrorMessageContains(String partialErrorMessage) {
        Objects.requireNonNull(partialErrorMessage, "partialErrorMessage");
        handleErrorLogs(logCaptures -> {
            Optional<String> errorMessage = RememberingLogger.getLastErrorMessage();
            errorMessage.ifPresent(lastErrorLogMessage -> {
                if (!lastErrorLogMessage.contains(partialErrorMessage)) {
                    throw new AssertionError("LogCaptureRule expected error message containing: "
                            + partialErrorMessage + " but was: " + lastErrorLogMessage);
                }
            });
            if (!errorMessage.isPresent()) {
                throw new LogCaptureRuleException(
                        "Expected error log message to contain: " + partialErrorMessage, null);
            }
        });
    }

    public void expectError(String message, int howManyMessagesBack) {
        Objects.requireNonNull(message, "message");
        handleErrorLogs(logCaptures -> {
            Optional<String> errorMessage = RememberingLogger.getErrorMessage(howManyMessagesBack);
            errorMessage.ifPresent(lastErrorLogMessage -> {
                if (!lastErrorLogMessage.equals(message)) {
                    throw new ComparisonFailure("LogCaptureRule expected different error message",
                            message, lastErrorLogMessage);
                }
            });
            if (!errorMessage.isPresent()) {
                throw new LogCaptureRuleException("Expected error log message: " + message, null);
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
