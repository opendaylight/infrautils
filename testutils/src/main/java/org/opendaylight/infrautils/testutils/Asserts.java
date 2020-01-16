/*
 * Copyright (c) 2002 - 2017 The JUnit Team. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import org.junit.Assert;

/**
 * Assert extension for JUnit.
 *
 * <p>Including some JUnit v5 NG APIs for JUnit 4 already.
 * @deprecated JUnit 4.13 ships with a working {@link Asserts#assertThrows(Class, JUnitExecutable)}, so there is no
 *             point shipping this class.
 */
@Deprecated(forRemoval = true)
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
     *
     * @deprecated Use {@link Assert#assertThrows(Class, org.junit.function.ThrowingRunnable)} instead.
     */
    @Deprecated(forRemoval = true)
    public static <T extends Throwable> T assertThrows(final Class<T> expectedThrowable, final JUnitExecutable exec) {
        return Assert.assertThrows(expectedThrowable, exec::execute);
    }

    /**
     * Functional interface that can be used to implement any generic block of code that potentially throws a Throwable.
     * Back-ported from <a href=
     * "http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/function/Executable.html">org.junit.jupiter.api.function.Executable</a>.
     */
    @Deprecated(forRemoval = true)
    @FunctionalInterface
    @SuppressWarnings("checkstyle:IllegalThrows")
    public interface JUnitExecutable {
        void execute() throws Throwable;
    }
}
