/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractInvocationHandler implements InvocationHandler {
    private static AtomicInteger hashCodeCounter = new AtomicInteger(0);
    private int hashValue;

    public AbstractInvocationHandler() {
        hashValue = hashCodeCounter.getAndIncrement();
    }

    abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("hashCode".equals(method.getName())) {
            return hashValue;
        }

        if ("equals".equals(method.getName())) {
            return equalsImpl(args);
        }

        return doInvoke(proxy, method, args);
    }

    private Object equalsImpl(Object[] args) {
        if (args.length != 1) {
            return false;
        }
        Object arg = args[0];
        if (arg == null) {
            return false;
        }
        if (!Proxy.isProxyClass(arg.getClass())) {
            return false;
        }

        return this == Proxy.getInvocationHandler(arg);
    }
}
