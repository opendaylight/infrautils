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
 * See {@link RunUntilFailureRule}.
 *
 * @author Michael Vorburger
 */
public class RunUntilFailureClassRule implements TestRule {

    private static final String HEADER = Strings.repeat("=", 120);

    // package local
    boolean isRunning = true;
    private final Long maximumNumberOfTimesToRun;

    public RunUntilFailureClassRule() {
        this.maximumNumberOfTimesToRun = null;
    }

    public RunUntilFailureClassRule(long maximumNumberOfTimesToRun) {
        if (maximumNumberOfTimesToRun < 1) {
            throw new IllegalArgumentException("maximumNumberOfTimesToRun must be positive");
        }
        this.maximumNumberOfTimesToRun = maximumNumberOfTimesToRun;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new RunUntilFailureStatement(statement, description);
    }

    private class RunUntilFailureStatement extends Statement {

        final Statement statement;
        final Logger testLog;

        RunUntilFailureStatement(Statement statement, Description description) {
            this.statement = statement;
            testLog = LoggerFactory.getLogger(description.getTestClass());
        }

        @Override
        public void evaluate() throws Throwable {
            int runNumber = 1;
            do {
                testLog.info(HEADER);
                testLog.info("RunUntilFailureRule #{}/{}", runNumber++,
                        maximumNumberOfTimesToRun == null ? "∞" : maximumNumberOfTimesToRun);
                statement.evaluate();
            }
            while (isRunning && (maximumNumberOfTimesToRun == null || runNumber < maximumNumberOfTimesToRun + 1));
        }

    }

}
