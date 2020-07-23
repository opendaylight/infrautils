/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import com.google.common.annotations.Beta;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
// In case you are wondering: yes, this is published to OSGi registry for the sake of diagstatus-shell. Not the grandest
// of ideas but it works for now.
// FIXME: merge with DiagStatusServiceMBeanImpl once we have OSGi DS with constructor injection
public final class OSGiDiagStatusServiceMBean implements DiagStatusServiceMBean {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDiagStatusServiceMBean.class);

    @Reference
    DiagStatusService diagStatusService = null;
    @Reference
    SystemReadyMonitor systemReadyMonitor = null;

    private DiagStatusServiceMBeanImpl delegate;

    @Override
    public String acquireServiceStatus() {
        return delegate.acquireServiceStatus();
    }

    @Override
    public String acquireServiceStatusDetailed() {
        return delegate.acquireServiceStatusDetailed();
    }

    @Override
    public String acquireServiceStatusBrief() {
        return delegate.acquireServiceStatusBrief();
    }

    @Override
    public String acquireServiceStatusAsJSON() {
        return delegate.acquireServiceStatusAsJSON();
    }

    @Override
    public Map<String, String> acquireServiceStatusMap() {
        return delegate.acquireServiceStatusMap();
    }

    @Activate
    void activate() throws JMException {
        delegate = new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor);
        LOG.info("Diagnostic Status Service management started");
    }

    @Deactivate
    void deactivate() throws InstanceNotFoundException, MBeanRegistrationException {
        delegate.close();
        LOG.info("Diagnostic Status Service management stopped");
    }
}
