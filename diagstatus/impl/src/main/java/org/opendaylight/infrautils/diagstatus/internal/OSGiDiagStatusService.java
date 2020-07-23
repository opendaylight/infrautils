/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, service = DiagStatusService.class)
public final class OSGiDiagStatusService extends AbstractDiagStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDiagStatusService.class);

    @Reference
    volatile List<ServiceStatusProvider> serviceStatusProviders = null;

    @Reference
    SystemReadyMonitor systemReadyMonitor = null;

    @Override
    Iterable<? extends ServiceStatusProvider> serviceStatusProviders() {
        return verifyNotNull(serviceStatusProviders);
    }

    @Override
    SystemReadyMonitor systemReadyMonitor() {
        return verifyNotNull(systemReadyMonitor);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Diagnostic Status Service started");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Diagnostic Status Service stopped");
    }
}
