/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import junit.framework.AssertionFailedError;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.Statement;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test of {@link LogCaptureRule} internals.
 * Necessary because while {@link LogCaptureRuleTest} illustrates the usage
 * and covers other scenario, it cannot actually directly test that LogCaptureRule really
 * does throw an exception, because Test expected = ... does not work with the LogCaptureRule
 * (due to the way JUnit works internally; the "expected" is checked BEFORE the Rule can throw).
 *
 * @author Michael Vorburger.ch
 */
@Ignore // TODO figure out why running each @Test separately (from IDE) passes, but all together last one fails..
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCaptureRuleInternalTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogCaptureRuleInternalTest.class);

    public @Rule LogRule logRule = new LogRule();

    private static LogCaptureRule LOG_CAPTURE_RULE = new LogCaptureRule();

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Test(expected = AssertionFailedError.class)
    public void testLogCaptureRule() throws Throwable {
        LOG_CAPTURE_RULE.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LOG.error("boum boum");
            }
        }, Description.createTestDescription(LogCaptureRuleInternalTest.class, "testLogCaptureRule"))
            .evaluate();
    }

    @Test
    @SuppressWarnings("checkstyle:IllegalThrows")
    public void testLogCaptureRuleExpectErrorPositive() throws Throwable {
        LOG_CAPTURE_RULE.expectError("boum");
        LOG_CAPTURE_RULE.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LOG.error("boum");
            }
        }, Description.createTestDescription(LogCaptureRuleInternalTest.class, "testLogCaptureRuleExpectErrorPositive"))
            .evaluate();
    }

    @Test(expected = AssertionFailedError.class)
    @SuppressWarnings("checkstyle:IllegalThrows")
    public void testLogCaptureRuleExpectErrorNegative() throws Throwable {
        LOG_CAPTURE_RULE.expectError("...");
        LOG_CAPTURE_RULE.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // do not LOG.error("boum")
            }
        }, Description.createTestDescription(LogCaptureRuleInternalTest.class, "testLogCaptureRuleExpectErrorNegative"))
            .evaluate();
    }

}
