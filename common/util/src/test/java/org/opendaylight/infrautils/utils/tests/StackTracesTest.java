/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.infrautils.utils.StackTraces;

/**
 * Unit Test for {@link StackTraces}.
 *
 * @author Michael Vorburger.ch
 */
public class StackTracesTest {
    @Test
    public void testGetCallerMethodName() {
        exampleMethodInsideSomeOtherUtility();
        assertEquals("testGetCallerMethodName", StackTraces.getCallerMethodName(0));
        // jdk.internal.reflect.NativeMethodAccessorImpl.invoke0()
        assertEquals("invoke0", StackTraces.getCallerMethodName(1));
    }

    private static void exampleMethodInsideSomeOtherUtility() {
        assertEquals("testGetCallerMethodName", StackTraces.getCallersCallerMethodName());
    }
}
