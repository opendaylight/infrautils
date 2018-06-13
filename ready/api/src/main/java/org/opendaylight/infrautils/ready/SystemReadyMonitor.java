/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Service which provides technical system ready-ness status.
 *
 * <p>This API is intentionally not specific to OSGi and Karaf, but using more
 * general terminology.
 * </p>
 * <p>In an OSGi context, "fully ready" means that the asynchronous installation of
 * (boot) features has successfully completed installation of all of their
 * bundles, that all of these bundles have successfully started, and all of
 * their blueprint containers have been successfully initialized.
 *</p>
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface SystemReadyMonitor extends SystemReadyMonitorMXBean {

    /**
     * Allows components to register a listener which will be notified when the system state is changing.
     * Each registered listener receives the current system state callback on registration.
     * @param listener
     *   instance of {@link SystemReadyListener}
     */
    void registerListener(SystemReadyListener listener);

    /**
     * Allows components to unregister a listener when it does not need do be notified about the
     * system state change or when the instance of the object is being destroyed.
     * @param listener
     *   instance of {@link SystemReadyListener}
     */
    void unregisterListener(SystemReadyListener listener);

}
