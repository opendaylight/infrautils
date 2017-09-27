/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
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
public class DiagStatusServiceImpl implements DiagStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();

    private final List<ServiceStatusProvider> serviceStatusProviders;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders) {
        this.serviceStatusProviders = serviceStatusProviders;
        LOG.info("{} initialized", getClass().getSimpleName());
    }

    @Override
    public ServiceRegistration register(String serviceIdentifier) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(serviceIdentifier, STARTING, "INITIALIZING");
        statusMap.put(serviceIdentifier, serviceDescriptor);
        return () -> {
            if (statusMap.remove(serviceIdentifier) == null) {
                throw new IllegalStateException("Service already unregistered");
            }
        };
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
        return statusMap.values();
    }

    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceReference : serviceStatusProviders) {
            ServiceDescriptor serviceDescriptor = serviceReference.getServiceDescriptor();
            statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
        }
    }
}
