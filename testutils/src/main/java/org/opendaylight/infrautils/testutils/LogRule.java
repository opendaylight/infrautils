/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

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

    private static final String HEADER = header(120);
    private static final String MESSAGE = "{} ({}ms) @Test {}() in {}";

    @Override
    public Statement apply(Statement statement, Description description) {
        Logger testLog = LoggerFactory.getLogger(description.getTestClass());
        return new Statement() {

            @Override
            @SuppressWarnings("checkstyle:IllegalCatch")
            public void evaluate() throws Throwable {
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
                        testLog.info(MESSAGE, "ENDED", durationInMS, description.getMethodName(),
                                description.getClassName());
                    } else {
                        testLog.info(MESSAGE, "FAILED", durationInMS, description.getMethodName(),
                                description.getClassName(), caughtThrowable);
                    }
                    testLog.info(HEADER);
                }
            }
        };
    }

    private static String header(int len) {
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            sb.append('=');
        }
        return sb.toString();
    }

}
