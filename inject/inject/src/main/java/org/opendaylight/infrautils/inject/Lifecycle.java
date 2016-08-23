/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Something which {@link #start()}s and {@link #stop()}s.
 *
 * <p>Annotated so that Dependency Injection Frameworks (whichever) automatically call these methods during wiring.
 *
 * @see LifecycleSupport
 * @see SingletonWithLifecycle
 *
 * @author Michael Vorburger
 */
public interface Lifecycle {

    @PostConstruct
    void start() throws ModuleSetupRuntimeException;

    @PreDestroy
    void stop() throws ModuleSetupRuntimeException;

    boolean isRunning();

}
