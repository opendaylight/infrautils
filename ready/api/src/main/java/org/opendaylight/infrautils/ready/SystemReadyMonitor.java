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
 *
 * <p>In an OSGi context, "fully ready" means that the asynchronous installation of
 * (boot) features has successfully completed installation of all of their
 * bundles, that all of these bundles have successfully started, and all of
 * their blueprint containers have been successfully initialized.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface SystemReadyMonitor {

    /**
     * Obtain current system status. Intended for showing to end-users on a CLI
     * or (REST API) Web UI, etc.
     *
     * The value
     * returned by this operation will frequently change during initial system
     * container boot.
     * <i>It may also change again later during dynamic feature
     * installation (this is not yet implemented).</i>
     *
     * This may be a single line or a much longer
     * multi-line String with extensive details about failures; in the latter case, it will
     * typically include technical information about
     * bundle states, detailed technical information related to OSGi bundle and blueprint
     * resolution, and possibly exceptions incl. stack traces.
     *
     * The same text
     * return by this call is also logged at level INFO by this service.
     */
    String getFullSystemStatus();

    /**
     * Obtain current system status as one of few possible enumerated values.
     */
    SystemState getSystemState();

    /**
     * Allows components to register a listener which will be notified when the
     * system ready state is changing.
     */
    void registerListener(SystemReadyListener listener);

    // TODO perhaps later move this to EventBus from https://git.opendaylight.org/gerrit/#/c/55852/

}
