/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice;

/**
 * Guice Module with classpath scanning based autowiring.
 *
 * @author Michael Vorburger.ch
 */
public class AutoWiringModule extends AbstractCheckedModule {

    protected final GuiceClassPathBinder classPathBinder;
    private final String packagePrefix;

    public AutoWiringModule(GuiceClassPathBinder classPathBinder, String packagePrefix) {
        this.classPathBinder = classPathBinder;
        this.packagePrefix = packagePrefix;
    }

    @Override
    protected final void checkedConfigure() throws Exception {
        classPathBinder.bindAllSingletons(packagePrefix, binder());
        configureMore();
    }

    protected void configureMore() throws Exception {
    }
}
