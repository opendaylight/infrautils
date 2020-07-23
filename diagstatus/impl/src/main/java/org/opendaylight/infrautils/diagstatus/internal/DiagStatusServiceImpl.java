/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusServiceImpl is the core class having the functionality for tracking the registered services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
public class DiagStatusServiceImpl extends AbstractDiagStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private final List<ServiceStatusProvider> serviceStatusProviders;

    private final SystemReadyMonitor systemReadyMonitor;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders,
            SystemReadyMonitor systemReadyMonitor) {
        this.systemReadyMonitor = systemReadyMonitor;
        this.serviceStatusProviders = serviceStatusProviders;
        LOG.info("{} started", getClass().getSimpleName());
    }

    @Override
    SystemReadyMonitor systemReadyMonitor() {
        return systemReadyMonitor;
    }

    @Override
    Iterable<? extends ServiceStatusProvider> serviceStatusProviders() {
        return serviceStatusProviders;
    }
}
