/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;
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
    private final @Nullable Long maximumNumberOfTimesToRun;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "TYPE_USE and SpotBugs")
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

    // findbugs-slf4j does not understand that a static final String IS a Constant, so SLF4J_FORMAT_SHOULD_BE_CONST
    // findbugs-slf4j cannot understand  what we are doing here with a Logger variable instead of class field, so:
    @SuppressFBWarnings({ "SLF4J_FORMAT_SHOULD_BE_CONST", "SLF4J_LOGGER_SHOULD_BE_PRIVATE" })
    private class RunUntilFailureStatement extends Statement {

        final Statement statement;
        final Logger testLog;

        RunUntilFailureStatement(Statement statement, Description description) {
            this.statement = statement;
            testLog = LoggerFactory.getLogger(description.getTestClass());
        }

        @Override
        public void evaluate() throws Throwable {
            @Var int runNumber = 1;
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
