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
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureClassRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example.
 *
 * @author Michael Vorburger
 */
public class ExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleTest.class);

    public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(10);
    public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    public @Rule LogRule logRule = new LogRule();

    @Test
    public void testA() throws Exception {
        LOG.info("doin' stuff in testA...");
        Thread.sleep(50);
    }

    @Test
    public void testB() throws Exception {
        LOG.info("doin' stuff in testB...");
        Thread.sleep(100);
        // fail("failure");
    }

}
