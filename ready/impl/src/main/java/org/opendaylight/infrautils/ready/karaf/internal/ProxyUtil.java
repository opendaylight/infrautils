/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;

/**
 * Utility for java.lang.reflect.Proxy.
 *
 * @author Michael Vorburger.ch
 */
final class ProxyUtil {
    // inspired by https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html

    // pre-loaded Method objects for the hashCode(), equals() and toString() methods in java.lang.Object
    private static final Method HASH_CODE_METHOD;
    private static final Method EQUALS_METHOD;
    private static final Method TO_STRING_METHOD;

    static {
        try {
            HASH_CODE_METHOD = Object.class.getMethod("hashCode");
            EQUALS_METHOD = Object.class.getMethod("equals", new Class[] { Object.class });
            TO_STRING_METHOD = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ProxyUtil() {
        // Hidden on purpose
    }

    @SuppressWarnings("unchecked")
    static <T extends FunctionalityReady> T newInstance(Class<T> markerInterface) {
        return (T) Proxy.newProxyInstance(markerInterface.getClassLoader(),
                new java.lang.Class[] { markerInterface }, new Delegator());
    }

    @SuppressWarnings("rawtypes")
    private static final class Delegator implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            Class declaringClass = method.getDeclaringClass();

            if (declaringClass == Object.class) {
                if (method.equals(HASH_CODE_METHOD)) {
                    return proxyHashCode(proxy);
                } else if (method.equals(EQUALS_METHOD)) {
                    return proxyEquals(proxy, args[0]);
                } else if (method.equals(TO_STRING_METHOD)) {
                    return proxyToString(proxy);
                } else {
                    throw new InternalError("unexpected Object method dispatched: " + method);
                }
            } else {
                return invokeNotDelegated(proxy, method, args);
            }
        }

        private static Object invokeNotDelegated(Object proxy, Method method, Object[] args) {
            throw new UnsupportedOperationException(
                    "FunctionalityReady interfaces should not have any methods: " + method);
        }

        private static int proxyHashCode(Object proxy) {
            return System.identityHashCode(proxy);
        }

        private static Boolean proxyEquals(Object proxy, Object other) {
            return proxy == other ? Boolean.TRUE : Boolean.FALSE;
        }

        private static String proxyToString(Object proxy) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        }
    }
}
