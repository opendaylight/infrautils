/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * Service which provides technical system ready-ness status.
 *
 * <p>This API is intentionally not specific to OSGi and Karaf, but using more
 * general terminology.
 *
 * <p>In an OSGi context, "fully ready" means that the asynchronous installation of
 * (boot) features has successfully completed installation of all of their
 * bundles, that all of these bundles have successfully started, and all of
 * their blueprint containers have been successfully initialized.
 *
 * <p>Implementations of this interface are expected to be thread-safe.
 *
 * @author Michael Vorburger.ch
 */
public interface SystemReadyMonitor extends SystemReadyMonitorMXBean {

    /**
     * Allows components to register a listener which will be notified when the
     * system ready state is changing.
     */
    void registerListener(SystemReadyListener listener);
}
