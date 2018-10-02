/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.shell;

import java.io.PrintStream;
import javax.annotation.Nullable;
import org.apache.karaf.shell.api.action.Action;
import org.slf4j.LoggerFactory;

/**
 * Base class to implement Karaf CLI command actions which correctly log failures.
 * Recommend because Karaf sometimes "swallows" (hides) exceptions (INFRAUTILS-55).
 *
 * @author Michael Vorburger.ch
 */
public abstract class LoggingAction implements Action {

    protected abstract void run(PrintStream ps) throws Exception;

    @Override
    @Nullable
    @SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpSinglelineJava"})
    public final Object execute() {
        try {
            run(System.out);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("{} CLI Action failed", getClass().getName(), e);
            System.out.println("Command FAILED (more details in the log) : " + e.getMessage());
        }
        return null;
    }
}
