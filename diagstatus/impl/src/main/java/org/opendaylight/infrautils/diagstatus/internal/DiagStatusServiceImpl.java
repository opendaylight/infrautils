/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static org.opendaylight.infrautils.diagstatus.ServiceState.ERROR;
import static org.opendaylight.infrautils.diagstatus.ServiceState.OPERATIONAL;
import static org.opendaylight.infrautils.diagstatus.ServiceState.STARTING;
import static org.opendaylight.infrautils.diagstatus.ServiceState.UNREGISTERED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
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

    @Inject
    public DiagStatusServiceImpl(SystemReadyMonitor systemReadyMonitor) {
        this.systemReadyMonitor = systemReadyMonitor;
        systemReadyMonitor.registerListener(this);
        LOG.info("{} started", getClass().getSimpleName());
    }

    @Override
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
    public void report(String service, ServiceState serviceState, String statusDescription) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(service, serviceState, statusDescription);
        statusMap.put(service, serviceDescriptor);
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceIdentifier) {
        updateServiceStatusMap(serviceIdentifier);
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
        for (String serviceIdentifier : statusMap.keySet()) {
            updateServiceStatusMap(serviceIdentifier.toString());
        }
    }

    private void updateServiceStatusMap(String serviceIdentifier) {
        ServiceDescriptor serviceDescriptor = statusMap.get(serviceIdentifier);
        ServiceDescriptor statusToBeUpdated;
        if (serviceDescriptor != null) {
            LOG.info("acquire service status for {}", serviceIdentifier);
            // TODO statusStr below will be actually polled from applications
            // TODO since this is not in place currently, just putting a TODO here
            String statusStr = "DUMMY";
            if (statusStr != null && statusStr.length() > 0) {
                // TODO poll this from applications who have registered for diagstatus service polling
            } else {
                LOG.error("Invalid service status received for {}", serviceIdentifier);
                statusToBeUpdated = new ServiceDescriptor(serviceIdentifier, ERROR,
                        "Invalid service status received");
                statusMap.put(serviceIdentifier, statusToBeUpdated);
            }
        } else {
            // SERVICE NOT REGISTERED
            LOG.error("Target Service {} is UNAVAILABLE for status check", serviceIdentifier);
            statusToBeUpdated = new ServiceDescriptor(serviceIdentifier, UNREGISTERED, null);
            statusMap.put(serviceIdentifier, statusToBeUpdated);
        }
    }

}
