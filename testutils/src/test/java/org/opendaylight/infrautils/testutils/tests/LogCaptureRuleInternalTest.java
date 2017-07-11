/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test of {@link LogCaptureRule} internals.
 * Necessary because while {@link LogCaptureRuleInternalTest} illustrates the usage
 * and covers other scenario, it cannot actually directly test that LogCaptureRule really
 * does throw an exception, because Test expected = ... does not work with the LogCaptureRule
 * (due to the way JUnit works internally; the "expected" is checked BEFORE the Rule can throw).
 *
 * @author Michael Vorburger.ch
 */
public class LogCaptureRuleInternalTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogCaptureRuleInternalTest.class);

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Test(expected = AssertionFailedError.class)
    public void testLogCaptureRule() throws Throwable {
        new LogCaptureRule().apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LOG.error("boum");
            }
        }, Description.EMPTY).evaluate();
    }
}
