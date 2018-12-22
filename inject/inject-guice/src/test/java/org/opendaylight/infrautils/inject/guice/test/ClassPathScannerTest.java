/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.test;

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.infrautils.inject.ClassPathScanner;

public class ClassPathScannerTest {

    private static final String PREFIX = "org.opendaylight.infrautils.inject.guice.test";

    @Test
    public void testClasspathScanning() {
        Set<Class<?>> singletons = new HashSet<>();
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        new ClassPathScanner(PREFIX).bindAllSingletons(PREFIX, bindings::put, singletons::add);
        assertThat(bindings).containsExactly(
                ClassPathScannerTestTopInterface.class, ClassPathScannerTestImplementation.class,
                ClassPathScannerTestAnotherInterface.class, ClassPathScannerTestImplementation.class);
        assertThat(singletons).containsExactly(ClassPathScannerTestNoInterfacesImplementation.class);
    }

    @Test
    public void testClasspathExclusion() {
        Set<Class<?>> singletons = new HashSet<>();
        Map<Class<?>, Class<?>> bindings = new HashMap<>();
        new ClassPathScanner(PREFIX).bindAllSingletons("nope", bindings::put, singletons::add);
        assertThat(bindings).isEmpty();
        assertThat(singletons).isEmpty();
    }
}
