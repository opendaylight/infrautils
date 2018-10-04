/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import java.util.List;
import java.util.Map;
import junit.framework.AssertionFailedError;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to run detect duplicate entries on the classpath. Usage:
 *
 * <pre>public static {@literal @}ClassRule ClasspathHellDuplicatesCheckRule
 *     dupes = new ClasspathHellDuplicatesCheckRule();</pre>
 *
 * <p>NB that the basepom/duplicate-finder-maven-plugin already runs as part of odlparent.
 * It has a similar purpose, but covers build time instead of runtime testing.  This JUnit Rule class is
 * thus recommended to be used in particular in tests which previously ran into JAR Hell issues, and for
 * which non-regression with a clear failure message in case of future similar problems is important.
 * (This provides more details at runtime than duplicate-finder-maven-plugin does at build time.)
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckRule implements TestRule {

    private final ClasspathHellDuplicatesChecker checker;

    public ClasspathHellDuplicatesCheckRule(ClasspathHellDuplicatesChecker checker) {
        this.checker = checker;
    }

    public ClasspathHellDuplicatesCheckRule() {
        this(ClasspathHellDuplicatesChecker.INSTANCE);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        checkClasspath();
        return base;
    }

    protected void checkClasspath() {
        Map<String, List<String>> dupes = checker.getDuplicates();
        if (!dupes.isEmpty()) {
            throw new AssertionFailedError("Classpath duplicates detected: " + checker.toString(dupes));
        }
    }
}
