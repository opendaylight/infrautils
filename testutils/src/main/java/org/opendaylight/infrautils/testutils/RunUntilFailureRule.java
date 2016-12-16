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
 * JUnit Rule which allows to keep running tests indefinitely.
 *
 * <p>This is useful to add locally, never commit, if you would like to
 * keep running a "flaky" (sometimes passing, sometimes failing) test
 * until it fails.
 *
 * <p>
 * Usage (NB the use of {@literal @}ClassRule instead of {@literal @}Rule):
 *
 * <pre>
 *   public {@literal @}ClassRule RunUntilFailureRule repeater = new RunUntilFailureRule();
 *
 *   {@literal @}Test ...
 * </pre>
 *
 * @author Michael Vorburger
 */
public class RunUntilFailureRule implements TestRule {

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
                testLog.info("RunUntilFailureRule #{}/∞", runNumber++);
                statement.evaluate();
            }
            while (true);
        }

    }

}
