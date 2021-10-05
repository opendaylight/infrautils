/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.function;

import static org.junit.Assert.assertThrows;

import java.io.FileNotFoundException;
import org.junit.Test;

/**
 * Unit Test for {@link CheckedCallable}.
 *
 * @author Michael Vorburger.ch
 */
public class CheckedRunnableTest {
    @Test
    public void testCheckedRunnableWithCheckedException() {
        assertThrows(FileNotFoundException.class, () -> foo(() -> {
            throw new FileNotFoundException("boum");
        }));
    }

    @Test
    public void testCheckedRunnableWithUncheckedException() {
        assertThrows(IllegalArgumentException.class, () -> foo(() -> {
            throw new IllegalArgumentException("boum");
        }));
    }

    @Test
    public void testCheckedRunnableWithoutAnyException() {
        foo(() -> { });
    }

    private static <E extends Exception> void foo(CheckedRunnable<E> checkedCallable) throws E {
        checkedCallable.run();
    }
}
