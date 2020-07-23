/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusServiceImpl is the core class having the functionality for tracking the registered services
 * and aggregating the status of the same.
 * @author Faseela K
 */
abstract class AbstractDiagStatusService implements DiagStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDiagStatusService.class);

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();

    @Override
    public final ServiceRegistration register(String serviceIdentifier) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(serviceIdentifier, STARTING, "INITIALIZING");
        statusMap.put(serviceIdentifier, serviceDescriptor);
        return () -> {
            if (statusMap.remove(serviceIdentifier) == null) {
                throw new IllegalStateException("Service already unregistered");
            }
        };
    }

    @Override
    public final void report(ServiceDescriptor serviceDescriptor) {
        statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
    }

    @Override
    public final ServiceDescriptor getServiceDescriptor(String serviceIdentifier) {
        updateServiceStatusMap();
        return statusMap.get(serviceIdentifier);
    }

    @Override
    public final ImmutableList<ServiceDescriptor> getAllServiceDescriptors() {
        updateServiceStatusMap();
        return ImmutableList.copyOf(statusMap.values());
    }

    @Override
    public final ServiceStatusSummary getServiceStatusSummary() {
        SystemReadyMonitor systemReadyMonitor = systemReadyMonitor();
        SystemState systemState = systemReadyMonitor.getSystemState();
        Collection<ServiceDescriptor> serviceDescriptors = getAllServiceDescriptors();
        return new ServiceStatusSummary(isOperational(systemState, serviceDescriptors),
                systemState, systemReadyMonitor.getFailureCause(), serviceDescriptors);
    }

    abstract Iterable<? extends ServiceStatusProvider> serviceStatusProviders();

    abstract SystemReadyMonitor systemReadyMonitor();

    private static boolean isOperational(SystemState systemState, Collection<ServiceDescriptor> serviceDescriptors) {
        if (!systemState.equals(SystemState.ACTIVE)) {
            return false;
        }
        for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
            if (!serviceDescriptor.getServiceState().equals(ServiceState.OPERATIONAL)) {
                return false;
            }
        }
        return true;
    }

    // because other projects implementing ServiceStatusProvider may not run FindBugs, we null check anyway
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceStatusProvider : serviceStatusProviders()) {
            ServiceDescriptor serviceDescriptor = serviceStatusProvider.getServiceDescriptor();
            if (serviceDescriptor != null) {
                statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
            } else {
                LOG.warn("ServiceStatusProvider getServiceDescriptor() returned null: {}", serviceStatusProvider);
            }
        }
    }
}
