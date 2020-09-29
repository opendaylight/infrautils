/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogCaptureRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test of {@link LogCaptureRule} internals.
 * Necessary because while {@link LogCaptureRuleInternalTest} illustrates the usage
 * and covers other scenarios, it cannot actually directly test that LogCaptureRule really
 * does throw an exception, because Test expected = ... does not work with the LogCaptureRule
 * (due to the way JUnit works internally; the "expected" is checked BEFORE the Rule can throw).
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRuleInternalTest {
    private static final Logger LOG = LoggerFactory.getLogger(LogCaptureRuleInternalTest.class);

    @Test
    public void testLogCaptureRule() {
        Statement stmt = new LogCaptureRule().apply(new Statement() {
            @Override
            public void evaluate() {
                LOG.error("boum");
            }
        }, Description.EMPTY);

        assertThrows(LogCaptureRuleException.class, () -> stmt.evaluate());
    }

    @Test
    public void testLogCaptureRuleNoErrLoggedButExceptionThrown() {
        Statement stmt = new LogCaptureRule().apply(new Statement() {
            @Override
            public void evaluate() {
                // do not log any errors, only throw some exception:
                throw new IllegalArgumentException("boum");
            }
        }, Description.EMPTY);

        assertThrows(IllegalArgumentException.class, () -> stmt.evaluate());
    }

    @Test
    public void testLogCaptureRuleExpectNoErrorButEncouterBothErrorLogAndException() {
        Statement stmt = new LogCaptureRule().apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LOG.error("boum logged message", new IllegalStateException("boum logged cause"));
                throw new Throwable("boum thrown message", new IllegalArgumentException("boum thrown cause"));
            }
        }, Description.EMPTY);

        LogCaptureRuleException ex = assertThrows(LogCaptureRuleException.class, () -> stmt.evaluate());
        // The point here is that we get "boum logged message" and not "boum thrown message" as (main) message.
        // That's useful because "boum logged" is typically the root cause of "boum thrown", and the developer
        // is best served by seeing that, first - especially if that was logged in a background thread!
        assertThat(ex.getMessage()).contains("boum logged message");
        // Likewise for any logged exceptions - that's what developers should see first in test failure cause
        assertThat(ex.getCause().toString()).isEqualTo(new IllegalStateException("boum logged cause").toString());
        // But we don't actually completely loose the "boum thrown" message & cause either:
        String stackTrace = getStackTrace(ex);
        assertThat(stackTrace).contains("boum thrown message");
        assertThat(stackTrace).contains("Suppressed");
        assertThat(stackTrace).contains(IllegalArgumentException.class.getName());
        assertThat(stackTrace).contains("boum thrown cause");
        // These x3 ^^^ asserts work because we preserved boum thrown in a suppressed exception!
    }

    @SuppressWarnings("checkstyle:RegexpSingleLineJava") // because printStackTrace is used to PrintWriter, not STDOUT
    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Test
    @SuppressWarnings("checkstyle:IllegalThrows")
    public void testLogCaptureRuleExpectErrorPositive() throws Throwable {
        LogCaptureRule logCaptureRule = new LogCaptureRule();
        logCaptureRule.expectError("boum");
        logCaptureRule.apply(new Statement() {
            @Override
            public void evaluate() {
                LOG.error("boum");
            }
        }, Description.EMPTY).evaluate();
    }

    @Test
    public void testLogCaptureRuleExpectErrorNegative() {
        LogCaptureRule logCaptureRule = new LogCaptureRule();
        logCaptureRule.expectError("...");
        Statement stmt = logCaptureRule.apply(new Statement() {
            @Override
            public void evaluate() {
                // do not LOG.error("boum")
            }
        }, Description.EMPTY);

        assertThrows(LogCaptureRuleException.class, () -> stmt.evaluate());
    }
}
