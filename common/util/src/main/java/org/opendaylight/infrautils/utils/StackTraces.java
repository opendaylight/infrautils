/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

/**
 * Utilities for {@link StackTraceElement} & Co.
 *
 * @author Michael Vorburger.ch, based on an idea by Stephen Kitt
 */
public class StackTraces {

    public static String getCallerMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            StackTraceElement element = stackTrace[2];
            return element.getMethodName();
        } else {
            // This normally should never happen...
            throw new IllegalStateException("Stack Trace is less than 2 levels deep?!");
        }
    }

    // TODO add (sth like) org.opendaylight.controller.md.sal.trace.dom.impl.TracingBroker's printStackTraceElements()

    // TODO add a suitable abstraction here which will allow impl to be switched to Java 9's JEP 259: Stack-Walking API

}
