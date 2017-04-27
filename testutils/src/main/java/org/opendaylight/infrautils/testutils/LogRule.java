/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.common.base.Strings;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Rule which nicely separates {@literal @}Test/s in the log.
 *
 * <p>
 * Usage (NB the use of {@literal @}Rule instead of {@literal @}ClassRule):
 *
 * <pre>
 *   public {@literal @}Rule LogRule logRule = new LogRule();
 *
 *   {@literal @}Test ...
 * </pre>
 *
 * @author Michael Vorburger
 */
public class LogRule implements TestRule {

    private static final String HEADER = Strings.repeat("-", 120);
    private static final String MESSAGE = "{} ({}ms) @Test {}()";

    @Override
    public Statement apply(Statement statement, Description description) {
        Logger testLog = LoggerFactory.getLogger(description.getTestClass());
        return new Statement() {

            @Override
            @SuppressWarnings("checkstyle:IllegalCatch")
            public void evaluate() throws Throwable {
                testLog.info(HEADER);
                testLog.info("BEGIN @Test {}()", description.getMethodName());
                long startTimeInMS = System.currentTimeMillis();
                Throwable caughtThrowable = null;
                try {
                    statement.evaluate();
                } catch (Throwable throwable) {
                    caughtThrowable = throwable;
                    throw throwable;
                } finally {
                    long durationInMS = System.currentTimeMillis() - startTimeInMS;
                    if (caughtThrowable == null) {
                        testLog.info(MESSAGE, "ENDED", durationInMS, description.getMethodName());
                    } else {
                        testLog.error(MESSAGE, "FAILED", durationInMS, description.getMethodName(), caughtThrowable);
                    }
                }
            }
        };
    }

}
