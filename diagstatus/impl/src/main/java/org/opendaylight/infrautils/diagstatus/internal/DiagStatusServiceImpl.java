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
import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
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
public class DiagStatusServiceImpl implements DiagStatusService, DiagStatusServiceImplMBean, SystemReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private static final String DEBUG_OUTPUT_FORMAT = "D";
    private static final String BRIEF_OUTPUT_FORMAT = "B";
    private static final String VERBOSE_OUTPUT_FORMAT = "V";
    private static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";

    private final SystemReadyMonitor systemReadyMonitor;

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();

    @Inject
    public DiagStatusServiceImpl(SystemReadyMonitor systemReadyMonitor) {
        MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
        this.systemReadyMonitor = systemReadyMonitor;
        systemReadyMonitor.registerListener(this);
        LOG.info("{} started", getClass().getSimpleName());
    }


    @Override
    @PreDestroy
    public void close() throws Exception {
        MBeanUtils.unregisterServerMBean(this, JMX_OBJECT_NAME);
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

    // ---
    // Following methods are the implementations of the MBean interface methods for acquiring service status

    @Override
    public String acquireServiceStatus() {
        updateServiceStatusMap();
        StringBuilder statusSummary = new StringBuilder();

        statusSummary.append("System ready state: ").append(getSystemState()).append('\n');

        for (Map.Entry<String, ServiceDescriptor> status : statusMap.entrySet()) {
            statusSummary.append("ServiceName          : ").append(status.getKey()).append("\n");
            if (status.getValue() != null) {
                if (status.getValue().getServiceState() != null) {
                    statusSummary.append("Last Reported Status : ")
                            .append(status.getValue().getServiceState().name()).append("\n");
                }
                if (status.getValue().getStatusDesc() != null) {
                    statusSummary.append("Reported Status Desc : ").append(status.getValue().getStatusDesc())
                            .append("\n");
                }
                if (status.getValue().getTimestamp() != null) {
                    statusSummary.append("Status Timestamp     : ").append(status.getValue()
                            .getTimestamp().toString()).append("\n\n");
                }
            } else {
                statusSummary.append("Not getting monitored         : \n");
            }
        }
        statusSummary.append("\n");

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusDetailed() {
        updateServiceStatusMap();
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System ready state: ").append(getSystemState()).append('\n');
        for (Map.Entry<String, ServiceDescriptor> status : statusMap.entrySet()) {
            if (status.getValue() != null) {
                statusSummary
                        .append("  ")
                        .append(String.format("%-20s%-20s", status.getKey(), ": "
                                + status.getValue().getServiceState()))
                        .append("\n");
            } else {
                statusSummary
                        .append("  ")
                        .append(String.format("%-20s%-20s", status.getKey(), ": ERROR"))
                        .append("\n");
            }
        }

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusBrief() {
        updateServiceStatusMap();
        StringBuilder statusSummary = new StringBuilder();
        String errorState = "ERROR - ";
        ServiceState state;

        statusSummary.append("System ready state: ").append(getSystemState()).append('\n');
        for (ServiceDescriptor stat : statusMap.values()) {
            state = stat.getServiceState();
            if (state.equals(ERROR) || state.equals(UNREGISTERED)) {
                statusSummary.append(errorState).append(stat.getModuleServiceName()).append(" ");
            }
        }

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusAsJSON(String outputType) {
        updateServiceStatusMap();
        return convertStatusSummaryToJSON(outputType);
    }

    @Override
    public Map<String, ServiceDescriptor> acquireServiceStatusMap() {
        updateServiceStatusMap();
        return ImmutableMap.copyOf(statusMap);
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

    private void updateServiceStatusMap() {
        for (String serviceIdentifier : statusMap.keySet()) {
            updateServiceStatusMap(serviceIdentifier.toString());
        }
    }

    private String convertStatusSummaryToJSON(String formatType) {
        String result = "{}";
        try {
            StringWriter strWrtr = new StringWriter();
            JsonWriter writer = new JsonWriter(strWrtr);
            writer.beginObject();
            writer.name("timeStamp").value(new Date().toString());
            writer.name("systemReadyState").value(getSystemState().name());
            writer.name("statusSummary");
            writer.beginArray(); //[
            for (Map.Entry<String, ServiceDescriptor> status : statusMap.entrySet()) {
                writer.beginObject(); // {
                if (formatType.equals(DEBUG_OUTPUT_FORMAT)) {
                    writer.name("serviceName").value(status.getKey());
                    writer.name("lastReportedStatus").value(status.getValue().getServiceState().name());
                    writer.name("effectiveStatus").value(status.getValue().getServiceState().name());
                    writer.name("reportedStatusDes").value(status.getValue().getStatusDesc());
                    writer.name("statusTimestamp").value(status.getValue().getTimestamp().toString());
                } else if (formatType.equals(VERBOSE_OUTPUT_FORMAT)) {
                    writer.name("serviceName").value(status.getKey());
                    writer.name("effectiveStatus").value(status.getValue().getServiceState().name());
                } else {
                    writer.name("statusBrief").value(acquireServiceStatusBrief());
                }
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.flush();
            writer.close();
            result = strWrtr.getBuffer().toString();
        } catch (IOException e) {
            LOG.error("Error while converting service status to JSON", e);
        }
        return result;
    }

}
