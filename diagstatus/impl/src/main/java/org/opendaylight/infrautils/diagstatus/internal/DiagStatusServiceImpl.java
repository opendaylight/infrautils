/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusServiceImpl is the core class having the functionality for tracking the registered services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
@Component(immediate = true, service = DiagStatusService.class)
public final class DiagStatusServiceImpl implements DiagStatusService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private final ConcurrentHashMap<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();
    private final SystemReadyMonitor systemReadyMonitor;

    private volatile List<ServiceStatusProvider> serviceStatusProviders;

    @Inject
    @Activate
    public DiagStatusServiceImpl(@Reference SystemReadyMonitor systemReadyMonitor,
            @Reference(policyOption = ReferencePolicyOption.GREEDY, updated = "setServiceStatusProviders")
            List<ServiceStatusProvider> serviceStatusProviders) {
        this.systemReadyMonitor = requireNonNull(systemReadyMonitor);
        LOG.info("Diagnostic Status Service started");
        setServiceStatusProviders(serviceStatusProviders);
    }

    synchronized void setServiceStatusProviders(List<ServiceStatusProvider> serviceStatusProviders) {
        this.serviceStatusProviders = List.copyOf(serviceStatusProviders);
    }

    @PreDestroy
    @Deactivate
    @Override
    public synchronized void close() {
        serviceStatusProviders = List.of();
        LOG.info("Diagnostic Status Service stopped");
    }

    @Override
    public ServiceRegistration register(String serviceIdentifier) {
        statusMap.put(serviceIdentifier, new ServiceDescriptor(serviceIdentifier, STARTING, "INITIALIZING"));
        return new ServiceRegistration() {
            @Override
            public void report(ServiceDescriptor serviceDescriptor) {
                var checked = requireNonNull(serviceDescriptor);
                var prev = statusMap.computeIfPresent(serviceIdentifier, (key, value) -> checked);
                if (prev == null) {
                    throw new IllegalStateException("Service already unregistered");
                }
            }

            @Override
            public void close() {
                statusMap.remove(serviceIdentifier);
            }
        };
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceIdentifier) {
        updateServiceStatusMap();
        return statusMap.get(serviceIdentifier);
    }

    @Override
    public ImmutableSet<ServiceDescriptor> getAllServiceDescriptors() {
        updateServiceStatusMap();
        return ImmutableSet.copyOf(statusMap.values());
    }

    @Override
    public ServiceStatusSummary getServiceStatusSummary() {
        var systemState = systemReadyMonitor.getSystemState();
        var serviceDescriptors = getAllServiceDescriptors();
        return new ServiceStatusSummary(isOperational(systemState, serviceDescriptors),
                systemState, systemReadyMonitor.getFailureCause(), serviceDescriptors);
    }

    // because other projects implementing ServiceStatusProvider may not run FindBugs, we null check anyway
    private void updateServiceStatusMap() {
        for (var serviceStatusProvider : serviceStatusProviders) {
            var serviceDescriptor = serviceStatusProvider.getServiceDescriptor();
            if (serviceDescriptor != null) {
                statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
            } else {
                LOG.warn("ServiceStatusProvider getServiceDescriptor() returned null: {}", serviceStatusProvider);
            }
        }
    }

    private static boolean isOperational(SystemState systemState, Collection<ServiceDescriptor> serviceDescriptors) {
        return systemState == SystemState.ACTIVE && serviceDescriptors.stream()
            .allMatch(serviceDescriptor -> serviceDescriptor.getServiceState() == ServiceState.OPERATIONAL);
    }
}
