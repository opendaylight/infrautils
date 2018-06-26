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
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.ops4j.pax.cdi.api.OsgiService;
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

    private final SystemReadyMonitor systemReadyMonitor;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders,
            @OsgiService SystemReadyMonitor systemReadyMonitor) {
        this.systemReadyMonitor = systemReadyMonitor;
        this.serviceStatusProviders = serviceStatusProviders;
        LOG.info("{} started", getClass().getSimpleName());
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
        return ImmutableList.copyOf(statusMap.values());
    }

    @Override
    public String getAllServiceDescriptorsAsJSON() {
        try (StringWriter stringWriter = new StringWriter()) {
            try (JsonWriter writer = new JsonWriter(stringWriter)) {
                writer.beginObject();
                writer.name("timeStamp").value(new Date().toString());
                writer.name("systemReadyState").value(systemReadyMonitor.getSystemState().name());
                writer.name("statusSummary");
                writer.beginArray();
                for (ServiceDescriptor status : getAllServiceDescriptors()) {
                    writer.beginObject();
                    writer.name("serviceName").value(status.getModuleServiceName());
                    writer.name("effectiveStatus").value(status.getServiceState().name());
                    writer.name("reportedStatusDescription").value(status.getStatusDesc());
                    writer.name("statusTimestamp").value(status.getTimestamp().toString());
                    writer.endObject();
                }
                writer.endArray();
                writer.endObject();
                writer.flush();
                writer.close();
                return stringWriter.getBuffer().toString();
            }
        } catch (IOException e) {
            LOG.error("Error while converting service status to JSON", e);
            return "{}";
        }
    }

    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceReference : serviceStatusProviders) {
            ServiceDescriptor serviceDescriptor = serviceReference.getServiceDescriptor();
            if (serviceDescriptor != null) {
                statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
            }
        }
    }

}
