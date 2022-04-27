/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import org.junit.Test;

public class MethodExtensionsTest {

    public <T> void fooBar(int index, T element) {
    }

    @Test
    public void betterToString() throws Exception {
        Method method = MethodExtensionsTest.class.getMethod("fooBar", Integer.TYPE, Object.class);
        assertEquals("fooBar(int index, T element)", MethodExtensions.toString(method));
    }
}
