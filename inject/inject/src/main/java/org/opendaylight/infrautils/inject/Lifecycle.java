/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

/**
 * Something which can be {@link #init()}-ialized and {@link #destroy()}-d.
 *
 * <p>Annotated so that Dependency Injection Frameworks (whichever) automatically call these methods during wiring.
 *
 * @see AbstractLifecycle
 *
 * @author Michael Vorburger
 */
public interface Lifecycle {

    /**
     * Initialize the object.
     *
     * @throws ModuleSetupRuntimeException if initialization failed
     */
    void init();

    /**
     * Destroy the object.
     *
     * @throws ModuleSetupRuntimeException if destruction failed
     */
    void destroy();

    boolean isRunning();

}
