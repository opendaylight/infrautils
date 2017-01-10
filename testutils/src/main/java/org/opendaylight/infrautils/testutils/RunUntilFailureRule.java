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

/**
 * JUnit Rule which allows to keep running tests indefinitely.
 *
 * <p>This is useful to add locally, never commit, if you would like to
 * keep running a "flaky" (sometimes passing, sometimes failing) test
 * until it fails. Usage:
 *
 * <pre>
 *   public static {@literal @}ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule();
 *   public {@literal @}Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);
 *
 *   {@literal @}Test ...</pre>
 *
 * <p>The two rules are necessary because a ClassRule alone cannot stop the test on a failure,
 * and a normal Rule alone cannot keep running all {@literal @}Test (it would indefinitely run
 * only the first test). Alternatives to two rules would be to use a {@literal @}RunWith
 * with a custom TestRunner, or writing a test suite each time.
 *
 * @author Michael Vorburger
 */
public class RunUntilFailureRule implements TestRule {

    private final RunUntilFailureClassRule classRepeaterRule;

    public RunUntilFailureRule(RunUntilFailureClassRule classRepeaterRule) {
        this.classRepeaterRule = classRepeaterRule;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new RunUntilFailureStatement(statement, description);
    }

    private class RunUntilFailureStatement extends Statement {

        final Statement statement;

        RunUntilFailureStatement(Statement statement, Description description) {
            this.statement = statement;
        }

        @Override
        @SuppressWarnings("checkstyle:IllegalCatch")
        public void evaluate() throws Throwable {
            try {
                statement.evaluate();
            } catch (Throwable throwable) {
                classRepeaterRule.isRunning = false;
                throw throwable;
            }
        }

    }

}
