/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static org.opendaylight.infrautils.diagstatus.ServiceState.OPERATIONAL;
import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusServiceImpl is the core class having the functionality for tracking the registered services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
@OsgiServiceProvider(classes = DiagStatusService.class)
public class DiagStatusServiceImpl implements DiagStatusService, SystemReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private final SystemReadyMonitor systemReadyMonitor;

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();

    private final List<ServiceStatusProvider> serviceStatusProviders;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders,
            SystemReadyMonitor systemReadyMonitor) {
        this.serviceStatusProviders = serviceStatusProviders;
        this.systemReadyMonitor = systemReadyMonitor;
        systemReadyMonitor.registerListener(this);
        LOG.info("{} started", getClass().getSimpleName());
    }

    @PreDestroy
    public void close() throws Exception {
        LOG.info("{} closed", getClass().getSimpleName());
    }

    @Override
    public boolean register(String serviceIdentifier) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(serviceIdentifier, STARTING,
                "INITIALIZING");
        statusMap.put(serviceIdentifier, serviceDescriptor);
        return true;
    }

    @Override
    public void onSystemBootReady() {
        for (Map.Entry<String, ServiceDescriptor> status : statusMap.entrySet()) {
            ServiceDescriptor serviceDescriptor = new ServiceDescriptor(status.getKey(), OPERATIONAL,
                    "Operational through global system readyness");
            status.setValue(serviceDescriptor);
        }
    }

    @Override
    public boolean deregister(String serviceIdentifier) {
        statusMap.remove(serviceIdentifier);
        return true;
    }

    @Override
    public void report(ServiceDescriptor serviceDescriptor) {
        statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceIdentifier) {
        updateServiceStatusMap();
        return statusMap.get(serviceIdentifier);
    }

    @Override
    public Collection<ServiceDescriptor> getAllServiceDescriptors() {
        updateServiceStatusMap();
        return ImmutableList.copyOf(statusMap.values());
    }

    @Override
    public SystemState getSystemState() {
        return systemReadyMonitor.getSystemState();
    }

    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceReference : serviceStatusProviders) {
            ServiceDescriptor serviceDescriptor = serviceReference.getServiceDescriptor();
            statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
        }
    }

}
