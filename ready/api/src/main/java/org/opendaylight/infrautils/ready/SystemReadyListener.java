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
    void onSystemBootReady();

    /**
     * Called back when the system becomes (temporarily, hopefully) 'un-ready'.
     * In an OSGi context like a Karaf container environment, this typically happens
     * the moment the end user operator types in e.g. 'feature:install oh-i-forgot-this-feature'
     * (or even 'bundle:install', as well as feature or bundle uninstall).
     * In a plain Java Environment, this typically does not occur.
     * <i>This is not yet implemented.</i>
     */
    // default void onSystemIsChanging() { }

    /**
     * Called back when the system has successfully converged to a stable state
     * and become 'ready again', following
     * {@link SystemReadyListener#onSystemIsChanging()}.
     * <i>This is not yet implemented.</i>
     */
    // default void onSystemReadyAgain() { }

}
