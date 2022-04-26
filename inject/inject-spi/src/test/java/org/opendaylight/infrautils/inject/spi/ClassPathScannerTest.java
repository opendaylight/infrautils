/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.spi;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class ClassPathScannerTest {
    private static final String PREFIX = ClassPathScannerTest.class.getPackageName();

    @Test
    public void testClasspathScanning() {
        Set<Class<?>> singletons = new HashSet<>();
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        new ClassPathScanner(PREFIX).bindAllSingletons(PREFIX, bindings::put, singletons::add);
        assertEquals(Map.of(
            ClassPathScannerTestTopInterface.class, ClassPathScannerTestImplementation.class,
            ClassPathScannerTestAnotherInterface.class, ClassPathScannerTestImplementation.class), bindings);
        assertEquals(Set.of(ClassPathScannerTestNoInterfacesImplementation.class), singletons);
    }

    @Test
    public void testClasspathExclusion() {
        Set<Class<?>> singletons = new HashSet<>();
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        new ClassPathScanner(PREFIX).bindAllSingletons("nope", bindings::put, singletons::add);
        assertEquals(Map.of(), bindings);
        assertEquals(Set.of(), singletons);
    }
}
