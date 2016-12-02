/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils;

/**
 * Convenience Guice module support class, which installs the
 * {@link AnnotationsModule}, and handles exceptions as the
 * {@link AbstractCheckedModule} does.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractGuiceJsr250Module extends AbstractCheckedModule {

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected final void checkedConfigure() throws Exception {
        install(new AnnotationsModule());
        configureBindings();
    }

    protected abstract void configureBindings() throws Exception;

}
