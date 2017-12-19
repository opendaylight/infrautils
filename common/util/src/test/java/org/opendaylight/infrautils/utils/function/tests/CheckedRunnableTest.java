/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.function.tests;

import java.io.FileNotFoundException;
import org.junit.Test;
import org.opendaylight.infrautils.utils.function.CheckedCallable;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;

/**
 * Unit Test for {@link CheckedCallable}.
 *
 * @author Michael Vorburger.ch
 */
public class CheckedRunnableTest {

    @Test(expected = FileNotFoundException.class)
    public void testCheckedRunnableWithCheckedException() throws FileNotFoundException {
        foo(() -> {
            throw new FileNotFoundException("boum");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckedRunnableWithUncheckedException() {
        foo(() -> {
            throw new IllegalArgumentException("boum");
        });
    }

    @Test
    public void testCheckedRunnableWithoutAnyException() {
        foo(() -> { });
    }

    private static <E extends Exception> void foo(CheckedRunnable<E> checkedCallable) throws E {
        checkedCallable.run();
    }
}
