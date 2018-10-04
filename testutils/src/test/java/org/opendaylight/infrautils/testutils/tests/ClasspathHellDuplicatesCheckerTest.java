/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesChecker;

/**
 * Unit test for {@link ClasspathHellDuplicatesChecker}.
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckerTest {

    @Test
    public void test() {
        // There should not be any duplicates
        assertThat(new ClasspathHellDuplicatesChecker().getDuplicates()).isEmpty();

        // Just to make sure that is actually does something, if we were not to filter anything, there would be some:
        assertThat(new ClasspathHellDuplicatesChecker() {
            @Override
            protected boolean isHarmlessDuplicate(String resourcePath) {
                return false;
            }
        }.getDuplicates().size()).isAtLeast(8);
    }
}
