/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import static org.junit.Assert.fail;

/**
 * Assert extension for JUnit.
 *
 * <p>Including some JUnit v5 NG APIs for JUnit 4 already.
 *
 * @author Michael Vorburger.ch, by way of shamelessly stealing from the Good People of JUnit 5
 */
public final class Asserts {

    private Asserts() {
    }

    /**
     * Asserts that execution of the supplied executable throws an exception of the
     * expectedType and returns the exception.
     *
     * <p>This is useful to avoid (and comply with) <a href=
     * "http://errorprone.info/bugpattern/TestExceptionChecker">Error Prone's TestExceptionChecker bug pattern</a>.
     *
     * <p>If no exception is thrown, or if an exception of a different type is thrown,
     * this method will fail.
     *
     * <p>If you do not want to perform additional checks on the exception instance,
     * simply ignore the return value.
     *
     * <p>Back-ported from <a href=
     * "http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertThrows-java.lang.Class-org.junit.jupiter.api.function.Executable-">org.junit.jupiter.api.Assertions</a>.
     */
    @SuppressWarnings({ "unchecked", "IllegalCatch" })
    public static <T extends Throwable> T assertThrows(Class<T> expectedType, JUnitExecutable exec) {
        try {
            exec.execute();
            fail("Threw nothing, expected to throw " + expectedType.getName());
            return null; // Java is too stupid to understand that fail() throws exception, so shut up compiler by this
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return (T) t;
            } else {
                fail("Threw " + t.getMessage() + ", but expected to throw " + expectedType.getName());
                return null; // Java is too stupid to understand that fail() throws exception, so shut up compiler
            }
        }
    }

    /**
     * Functional interface that can be used to implement any generic block of code that potentially throws a Throwable.
     * Back-ported from <a href=
     * "http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/function/Executable.html">org.junit.jupiter.api.function.Executable</a>.
     */
    @FunctionalInterface
    @SuppressWarnings("checkstyle:IllegalThrows")
    public interface JUnitExecutable {
        void execute() throws Throwable;
    }

}
