/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesChecker;

/**
 * Unit test for {@link ClasspathHellDuplicatesChecker}.
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckerTest {
    @Test
    public void testNoDuplicates() {
        assertEquals(Map.of(), new ClasspathHellDuplicatesChecker().getDuplicates());
    }

    @Test
    public void testDuplicates() {
        // Just to make sure that is actually does something, if we were not to filter anything, there would be some:
        assertFalse(new ClasspathHellDuplicatesChecker() {
            @Override
            protected boolean isHarmlessDuplicate(String resourcePath) {
                return false;
            }
        }.getDuplicates().isEmpty());
    }
}
