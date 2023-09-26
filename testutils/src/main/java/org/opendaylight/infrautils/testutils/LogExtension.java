/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.common.base.Stopwatch;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * JUnit Rule which nicely separates {@literal @}Test/s in the log.
 *
 * <p>
 * Usage (NB the use of {@literal @}Rule instead of {@literal @}ClassRule):
 *
 * <pre>
 *   {@code
 *     @ExtendWith(LogExtension.class);
 *     class TestClass {
 *       // ...
 *     }
 *   }
 * </pre>
 */
public final class LogExtension implements InvocationInterceptor {
    static final String HEADER = "-".repeat(120);
    private static final Marker MARKER = MarkerFactory.getMarker(LogExtension.class.getName());

    public static Marker marker() {
        return MARKER;
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        var testLog = LoggerFactory.getLogger(invocationContext.getTargetClass());
        var methodName = invocationContext.getExecutable().getName();

        testLog.info(MARKER, HEADER);
        testLog.info(MARKER, "BEGIN @Test {}()", methodName);
        var sw = Stopwatch.createStarted();
        try {
            invocation.proceed();
        } catch (Throwable e) {
            testLog.error(MARKER, "FAILED ({}) @Test {}()", sw, methodName, e);
            throw e;
        }
        testLog.info(MARKER, "ENDED ({}) @Test {}()", sw, methodName);
    }
}
