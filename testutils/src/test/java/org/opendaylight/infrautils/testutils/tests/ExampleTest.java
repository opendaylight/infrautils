/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesCheckRule;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureClassRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test, and example, of how to use the different new JUnit Rules.
 *
 * @author Michael Vorburger
 */
public class ExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleTest.class);

    @ClassRule public static RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(10);
    @Rule public RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    @Rule public LogRule logRule = new LogRule();
    @Rule public LogCaptureRule logCaptureRule = new LogCaptureRule();

    @ClassRule public static ClasspathHellDuplicatesCheckRule jHades = new ClasspathHellDuplicatesCheckRule();

    @Test
    public void testA() throws Exception {
        LOG.info("doin' stuff in testA...");
        Thread.sleep(50);
        // LOG.error("boum");
    }

    @Test
    public void testB() throws Exception {
        LOG.info("doin' stuff in testB...");
        Thread.sleep(100);
        // fail("failure");
    }

}
