/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opendaylight.infrautils.utils.mdc.ExecutionOrigin;
import org.opendaylight.infrautils.utils.mdc.MDCs;

/**
 * TODO Doc.
 *
 * @author Michael Vorburger.ch
 */
public class ExecutionOriginRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                MDCs.putRunCheckedRemove(ExecutionOrigin.next(), () -> {
                    statement.evaluate();
                });
            }
        };
    }

}
