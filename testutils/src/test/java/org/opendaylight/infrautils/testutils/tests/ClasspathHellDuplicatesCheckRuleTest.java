/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import org.junit.ClassRule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesCheckRule;

/**
 * {@link ClasspathHellDuplicatesCheckRule} test.
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
