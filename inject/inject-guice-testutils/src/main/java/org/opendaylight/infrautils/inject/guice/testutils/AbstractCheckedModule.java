/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils;

import com.google.inject.AbstractModule;
import org.opendaylight.infrautils.inject.ModuleSetupRuntimeException;

/**
 * Convenience Guice module support class with configure method that allows
 * throwing checked exceptions, which are caught and re-thrown as unchecked
 * {@link ModuleSetupRuntimeException}.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCheckedModule extends AbstractModule {

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected final void configure() throws ModuleSetupRuntimeException {
        try {
            checkedConfigure();
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

    protected abstract void checkedConfigure() throws Exception;

}
