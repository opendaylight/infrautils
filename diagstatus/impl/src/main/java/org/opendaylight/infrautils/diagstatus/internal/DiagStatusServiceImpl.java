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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
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
public class DiagStatusServiceImpl implements DiagStatusService, DiagStatusServiceImplMBean, SystemReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private static final String DEBUG_OUTPUT_FORMAT = "D";
    private static final String BRIEF_OUTPUT_FORMAT = "B";
    private static final String VERBOSE_OUTPUT_FORMAT = "V";
    private static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";

    private final SystemReadyMonitor systemReadyMonitor;

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();

    private final List<ServiceStatusProvider> serviceStatusProviders;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders,
            @OsgiService SystemReadyMonitor systemReadyMonitor) {
        this.serviceStatusProviders = serviceStatusProviders;
        MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
        this.systemReadyMonitor = systemReadyMonitor;
        systemReadyMonitor.registerListener(this);
        LOG.info("{} started", getClass().getSimpleName());
    }

    @PreDestroy
    public void close() {
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
    public Map<String, String> acquireServiceStatusMap() {
        updateServiceStatusMap();
        Map<String, String> resultMap = new HashMap<>();
        if (statusMap.size() > 0) {
            for (ServiceDescriptor stat : statusMap.values()) {
                String operationalState;
                ServiceState state = stat.getServiceState();
                if (state == null || state.equals(ServiceState.ERROR) || state.equals(ServiceState.UNREGISTERED)) {
                    operationalState = "ERROR";
                } else {
                    operationalState = "OPERATIONAL";
                }
                resultMap.put(stat.getModuleServiceName(), operationalState);
            }
        }
        return ImmutableMap.copyOf(resultMap);
    }

    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceReference : serviceStatusProviders) {
            ServiceDescriptor serviceDescriptor = serviceReference.getServiceDescriptor();
            statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
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
                if (formatType.equals(MBeanUtils.DEBUG_OUTPUT_FORMAT)) {
                    writer.name("serviceName").value(status.getKey());
                    writer.name("lastReportedStatus").value(status.getValue().getServiceState().name());
                    writer.name("effectiveStatus").value(status.getValue().getServiceState().name());
                    writer.name("reportedStatusDes").value(status.getValue().getStatusDesc());
                    writer.name("statusTimestamp").value(status.getValue().getTimestamp().toString());
                } else if (formatType.equals(MBeanUtils.VERBOSE_OUTPUT_FORMAT)) {
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
