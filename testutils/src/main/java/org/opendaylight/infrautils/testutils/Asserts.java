/*
 * Copyright (c) 2002 - 2017 The JUnit Team. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.errorprone.annotations.Var;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Assert extension for JUnit.
 *
 * <p>Including some JUnit v5 NG APIs for JUnit 4 already.
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
    @SuppressWarnings({ "IllegalCatch", "AvoidHidingCauseException" }) // OK here
    public static <T extends Throwable> T assertThrows(Class<T> expectedThrowable, JUnitExecutable exec) {
        //
        // This implementation, and the following private methods, are copy/paste'd verbatim from
        // https://github.com/junit-team/junit4/blob/master/src/main/java/org/junit/Assert.java
        // which is fine because that is under EPL just like this
        //
        try {
            exec.execute();
        } catch (Throwable actualThrown) {
            if (expectedThrowable.isInstance(actualThrown)) {
                return expectedThrowable.cast(actualThrown);
            }

            @Var String expected = formatClass(expectedThrowable);
            Class<? extends Throwable> actualThrowable = actualThrown.getClass();
            @Var String actual = formatClass(actualThrowable);
            if (expected.equals(actual)) {
                // There must be multiple class loaders. Add the identity hash code so the message
                // doesn't say "expected: java.lang.String<my.package.MyException> ..."
                expected += "@" + Integer.toHexString(System.identityHashCode(expectedThrowable));
                actual += "@" + Integer.toHexString(System.identityHashCode(actualThrowable));
            }
            String mismatchMessage = format("unexpected exception type thrown;", expected, actual);

            // The AssertionError(String, Throwable) ctor is only available on JDK7.
            AssertionError assertionError = new AssertionError(mismatchMessage);
            assertionError.initCause(actualThrown);
            throw assertionError;
        }
        String message = String.format("expected %s to be thrown, but nothing was thrown",
                formatClass(expectedThrowable));
        throw new AssertionError(message);
    }

    private static boolean isEquals(Object expected, @Nullable Object actual) {
        return expected.equals(actual);
    }

    private static boolean equalsRegardingNull(@Nullable Object expected, @Nullable Object actual) {
        return expected == null ? actual == null : isEquals(expected, actual);
    }

    private static String format(String message, Object expected, Object actual) {
        @Var String formatted = "";
        if (message != null && !"".equals(message)) {
            formatted = message + " ";
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (equalsRegardingNull(expectedString, actualString)) {
            return formatted + "expected: "
                    + formatClassAndValue(expected, expectedString)
                    + " but was: " + formatClassAndValue(actual, actualString);
        }

        return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
    }

    private static String formatClass(Class<?> value) {
        String className = value.getCanonicalName();
        return className == null ? value.getName() : className;
    }

    private static String formatClassAndValue(Object value, String valueString) {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
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
