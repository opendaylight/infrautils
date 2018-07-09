/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * Listener which can get notified when the system ready state is changing.
 *
 * @see SystemReadyMonitor
 *
 * @author Michael Vorburger.ch
 */
public interface SystemReadyListener {

    /**
     * Called back once when the system has become "fully ready" after the
     * initial boot up. In an OSGi context like a Karaf container environment,
     * this means after all "boot features" have successfully loaded. In a plain
     * Java Environment including in unit and component tests, this means after
     * dependency injection object wiring (incl. invocation of all
     * {@literal @}PostConstruct "init" type methods) has completed.
     */
    @Deprecated
    void onSystemBootReady();

    /**
     * Called back on system state change.
     * @param systemState
     *   Current system state.
     */
    default void onSystemStateChange(SystemState systemState) {
        if (systemState.equals(SystemState.ACTIVE)) {
            onSystemBootReady();
        }
    }

}
