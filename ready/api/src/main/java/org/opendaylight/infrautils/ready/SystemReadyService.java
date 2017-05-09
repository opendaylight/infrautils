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
public interface SystemReadyService {

    String FULLY_READY = "System is fully ready";

    /**
     * Obtain current system status. Intended for showing to end-users on a CLI
     * or (REST API) Web UI, etc. This may be a single line or a much longer
     * multi-line String with extensive details about failures etc. The value
     * returned by this operation will frequently change during initial system
     * container boot, and typically include human readable information about
     * bundle states, exceptions incl. stack traces in case of failures, or
     * detailed technical information related to OSGi bundle and blueprint
     * resolution. It may also change again later during dynamic feature
     * installation (this is not yet implemented). When the system is in a
     * stable "fully ready" state, then this will return the fixed
     * {@link #FULLY_READY} constant.
     */
    String getFullSystemStatus();

    /**
     * Allows components to register a listener which will be notified when the
     * system is "fully ready".
     */
    void registerListener(SystemReadyListener listener);

    // TODO perhaps later move this to EventBus from https://git.opendaylight.org/gerrit/#/c/55852/

}
