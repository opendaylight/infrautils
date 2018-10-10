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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for java.lang.reflect.Proxy.
 *
 * @author Michael Vorburger.ch
 */
final class ProxyUtil {

    private ProxyUtil() { }

    private static final Logger LOG = LoggerFactory.getLogger(ProxyUtil.class);

    // inspired by https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html

    // pre-loaded Method objects for the hashCode(), equals() and toString() methods in java.lang.Object
    private static Method hashCodeMethod;
    private static Method equalsMethod;
    private static Method toStringMethod;

    static {
        initializeMethods();
    }

    @SuppressWarnings("checkstyle:AvoidHidingCauseException")
    private static void initializeMethods() {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod =
                Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            LOG.error("getMethod() failed", e);
            NoSuchMethodError noSuchMethodError = new NoSuchMethodError(e.getMessage());
            noSuchMethodError.initCause(e);
            throw noSuchMethodError;
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends FunctionalityReady> T newInstance(Class<T> markerInterface) {
        return (T) Proxy.newProxyInstance(markerInterface.getClassLoader(),
                new java.lang.Class[] { markerInterface }, new Delegator());
    }

    @SuppressWarnings("rawtypes")
    private static final class Delegator implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class declaringClass = method.getDeclaringClass();

            if (declaringClass == Object.class) {
                if (method.equals(hashCodeMethod)) {
                    return proxyHashCode(proxy);
                } else if (method.equals(equalsMethod)) {
                    return proxyEquals(proxy, args[0]);
                } else if (method.equals(toStringMethod)) {
                    return proxyToString(proxy);
                } else {
                    throw new InternalError("unexpected Object method dispatched: " + method);
                }
            } else {
                return invokeNotDelegated(proxy, method, args);
            }
        }

        protected Object invokeNotDelegated(Object proxy, Method method, Object[] args)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "FunctionalityReady interfaces should not have any methods: " + method);
        }

        protected Integer proxyHashCode(Object proxy) {
            return Integer.valueOf(System.identityHashCode(proxy));
        }

        protected Boolean proxyEquals(Object proxy, Object other) {
            return proxy == other ? Boolean.TRUE : Boolean.FALSE;
        }

        protected String proxyToString(Object proxy) {
            return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
        }
    }

}
