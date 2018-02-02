/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.lastnpe.tests;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.infrautils.utils.lastnpe.NonNulls;

/**
 * Unit test for {@link NonNulls}.
 *
 * @author Michael Vorburger.ch
 */
public class NonNullsTest {

    @Test
    @SuppressWarnings("unused")
    public void testAtomicReference() {
        AtomicReference<String> atomicReference = new AtomicReference<>("");
        @Nullable String nullableString = atomicReference.get();
        String nonNullString = NonNulls.castToNonNull(atomicReference.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        NonNulls.castToNonNull(null);
    }

    public void testNonNull() {
        assertThat(NonNulls.castToNonNull("hello, world")).isEqualTo("hello, world");
    }

}
