/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.tests;

import org.junit.ClassRule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesCheckRule;

/**
 * {@link ClasspathHellDuplicatesCheckRule} test.
 *
 * <p>This test is copy/pasted from the identical one in testutils,
 * because the one there fails to find duplicates due to itself, like
 * the StaticLoggerBinder.class in testutils for the LogCaptureRule.
 *
 * @author Michael Vorburger
 */
public class ClasspathHellDuplicatesCheckRuleTest {

    @ClassRule public static ClasspathHellDuplicatesCheckRule jHades = new ClasspathHellDuplicatesCheckRule();

    @Test
    public void testSomething() throws Exception {
        // OK!
    }

}
