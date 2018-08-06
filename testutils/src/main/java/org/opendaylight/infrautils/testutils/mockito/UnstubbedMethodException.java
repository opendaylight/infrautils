/*
 * Copyright (c) 2016 Red Hat and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import java.lang.reflect.Method;
import org.mockito.internal.util.MockUtil;

/**
 * Exception to be thrown on unstubbed method calls.
 *
 * @author Michael Vorburger
 */
public class UnstubbedMethodException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    // constructors are intentionally kept package private

    UnstubbedMethodException(Method method) {
        super(MethodExtensions.toString(method) + " is not stubbed in mock of " + method.getDeclaringClass().getName());
    }

    UnstubbedMethodException(Method method, Object mockAbstractFakeObject) {
        super(MethodExtensions.toString(method) + " is not implemented in "
                + MockUtil.getMockName(mockAbstractFakeObject));
    }
}
