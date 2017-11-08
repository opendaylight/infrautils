/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.function.tests;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileNotFoundException;
import org.junit.Test;
import org.opendaylight.infrautils.utils.function.CheckedCallable;

/**
 * Unit Test for {@link CheckedCallable}.
 *
 * @author Michael Vorburger.ch
 */
public class CheckedCallableTest {

    @Test(expected = FileNotFoundException.class)
    public void testCheckedCallableWithCheckedException() throws FileNotFoundException {
        foo(() -> {
            throw new FileNotFoundException("boum");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckedCallableWithUncheckedException() {
        foo(() -> {
            throw new IllegalArgumentException("boum");
        });
    }

    @Test
    public void testCheckedCallableWithoutAnyException() {
        assertThat(foo(() -> 43)).isEqualTo(43);
    }

    private <T, E extends Exception> T foo(CheckedCallable<T, E> checkedCallable) throws E {
        return checkedCallable.call();
    }
}
