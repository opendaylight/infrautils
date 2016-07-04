/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

import com.google.common.util.concurrent.SettableFuture;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

public class MethodCallMessage {
    Method method;
    Object[] args;
    SettableFuture<Object> result;

    public MethodCallMessage(Method method, Object... args) {
        this.method = method;
        this.args = args;

        if (method.getReturnType().equals(Future.class)) {
            result = SettableFuture.create();
        }
    }
}
