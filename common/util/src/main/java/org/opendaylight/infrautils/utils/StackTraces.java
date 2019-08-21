/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

/**
 * Utilities for {@link StackTraceElement} &amp; Co.
 *
 * @author Michael Vorburger.ch, based on an idea by Stephen Kitt
 */
public final class StackTraces {
    private StackTraces() {

    }

    /**
     * Obtain the name of the method that is the method calling this one. This is
     * just a short cut for <code>getCallerMethodName(2)</code>, and is typically
     * what one wants when using this from another utility.
     *
     * @return name of calling Java method
     */
    public static String getCallersCallerMethodName() {
        return getCallerMethodName(2);
    }

    /**
     * Obtain the name of the method that is depth N way up the call stack from
     * invoking this. So e.g. depth 0 is the name of the method calling this; but
     * is rarely what one wants - as that is already known at that point. Typically
     * depth 1 or 2 is what one intends.
     *
     * @param depth depth into call stack
     * @return name of calling Java method
     */
    public static String getCallerMethodName(int depth) {
        // Add +2 to compensate for call into this method & getStackTrace() invocation:
        int realDepth = depth + 2;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > realDepth) {
            StackTraceElement element = stackTrace[realDepth];
            return element.getMethodName();
        } else {
            // This normally should never happen...
            throw new IllegalStateException("Stack Trace is less than " + realDepth + " levels deep?!");
        }
    }

    // TODO add (sth like) org.opendaylight.controller.md.sal.trace.dom.impl.TracingBroker's printStackTraceElements()

    // TODO add a suitable abstraction here which will allow impl to be switched to Java 9's JEP 259: Stack-Walking API
    // by looking at e.g. what org.opendaylight.controller.md.sal.trace.closetracker.impl.CloseTrackedTrait does

}
