/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 * Copyright (c) 2023 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit Rule to run detect duplicate entries on the classpath. Usage:
 *
 * <pre>
 *   {@code
 *     @ExtendWith(ClasspathHellDuplicatesCheckExtension.class)
 *     class Test class {
 *       // ..
 *     }
 *   }
 * </pre>
 *
 * <p>NB that the basepom/duplicate-finder-maven-plugin already runs as part of odlparent.
 * It has a similar purpose, but covers build time instead of runtime testing.  This JUnit Rule class is
 * thus recommended to be used in particular in tests which previously ran into JAR Hell issues, and for
 * which non-regression with a clear failure message in case of future similar problems is important.
 * (This provides more details at runtime than duplicate-finder-maven-plugin does at build time.)
 */
public final class ClasspathHellDuplicatesCheckExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        var dupes = new ClasspathHellDuplicatesChecker().getDuplicates();
        if (!dupes.isEmpty()) {
            AssertionFailureBuilder.assertionFailure()
                .message("Classpath duplicates detected: " + ClasspathHellDuplicatesChecker.toString(dupes))
                .buildAndThrow();
        }
    }
}
