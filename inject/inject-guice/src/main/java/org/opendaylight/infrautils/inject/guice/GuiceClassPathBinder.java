/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice;

import com.google.inject.Binder;
import javax.inject.Singleton;
import org.opendaylight.infrautils.inject.ClassPathScanner;

/**
 * Binds interfaces to implementations in Guice by scanning the classpath.
 */
public class GuiceClassPathBinder {
    private final ClassPathScanner scanner;

    public GuiceClassPathBinder(String prefix) {
        this.scanner = new ClassPathScanner(prefix);
    }

    /**
     * Binds all {@link Singleton} annotated classes discovered by scanning the class path to all their interfaces.
     *
     * @param prefix the package prefix of Singleton implementations to consider
     * @param binder The binder to set up.
     */
    @SuppressWarnings("unchecked")
    public void bindAllSingletons(String prefix, Binder binder) {
        scanner.bindAllSingletons(prefix,
            (contract, implementation) -> binder.bind(contract).to(implementation),
            singleton -> binder.bind(singleton));
    }
}
