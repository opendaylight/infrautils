/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.tests;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.Asserts;

/**
 * Unit Test for {@link Asserts} utility.
 *
 * @author Michael Vorburger.ch
 */
@Deprecated(forRemoval = true)
public class AssertsTest {

    @Test
    public void testPassingAssertThrows() {
        AtomicBoolean isCalled = new AtomicBoolean(false);
        IOException thrown = Asserts.assertThrows(IOException.class, () -> {
            isCalled.set(true);
            throw new IOException();
        });
        assertThat(thrown).isNotNull();
        assertThat(isCalled.get()).isTrue();
    }

    @Test(expected = AssertionError.class)
    public void testFailingAssertThrows() {
        Asserts.assertThrows(IOException.class, () -> {
            // NOOP
        });
    }

}
