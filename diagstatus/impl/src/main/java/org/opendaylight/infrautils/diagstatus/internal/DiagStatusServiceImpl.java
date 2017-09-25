/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.internal;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
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
public class DiagStatusServiceImpl implements DiagStatusService, DiagStatusServiceImplMBean {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);

    private final Map<String, ServiceDescriptor> statusMap = new ConcurrentHashMap<>();
    private final List<ServiceStatusProvider> serviceStatusProviders;

    @Inject
    public DiagStatusServiceImpl(List<ServiceStatusProvider> serviceStatusProviders) {
        this.serviceStatusProviders = serviceStatusProviders;
        LOG.info("{} initialized", getClass().getSimpleName());
    }

    @PostConstruct
    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
        MBeanUtils.registerServerMBean(this, MBeanUtils.JMX_OBJECT_NAME);
    }

    @Override
    @PreDestroy
    public void close() throws Exception {
        MBeanUtils.unregisterServerMBean(this, MBeanUtils.JMX_OBJECT_NAME);
        LOG.info("{} close", getClass().getSimpleName());
    }

    @Override
    public boolean register(String serviceIdentifier) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(serviceIdentifier, ServiceState.STARTING,
                "INITIALIZING");
        statusMap.put(serviceIdentifier, serviceDescriptor);
        return true;
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
    public void report(String service, ServiceState serviceState, String statusDescription) {
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor(service, serviceState, statusDescription);
        statusMap.put(service, serviceDescriptor);
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

    /*
     *  MBean interface Implementations for acquiring service status
     */
    @Override
    public String acquireServiceStatus() {
        updateServiceStatusMap();
        StringBuilder statusSummary = new StringBuilder();

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
        for (Map.Entry<String, ServiceDescriptor> status : statusMap.entrySet()) {
            if (status.getValue() != null) {
                statusSummary
                        .append("  ")
                        .append(String.format("%-20s%-20s", status.getKey(), ": "
                                + status.getValue().getServiceState().name()))
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

        for (ServiceDescriptor stat : statusMap.values()) {
            state = stat.getServiceState();
            if (state.equals(ServiceState.ERROR) || state.equals(ServiceState.UNREGISTERED)) {
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
        java.util.HashMap resultMap = new java.util.HashMap();
        String operationalState;
        if (statusMap.size() > 0) {
            for (ServiceDescriptor stat : statusMap.values()) {
                ServiceState state = stat.getServiceState();
                if (state == null || state.equals(ServiceState.ERROR) || state.equals(ServiceState.UNREGISTERED)) {
                    operationalState = "ERROR";
                } else {
                    operationalState = "OPERATIONAL";
                }
                resultMap.put(stat.getModuleServiceName(), operationalState);
            }
        }
        return resultMap;
    }

    private void updateServiceStatusMap() {
        for (ServiceStatusProvider serviceReference : serviceStatusProviders) {
            ServiceDescriptor serviceDescriptor = serviceReference.getServiceDescriptor();
            statusMap.put(serviceDescriptor.getModuleServiceName(), serviceDescriptor);
        }
    }

    private String convertStatusSummaryToJSON(String formatType) {
        String result = "{}";
        StringWriter strWrtr = new StringWriter();
        JsonWriter writer;
        try {
            writer = new JsonWriter(strWrtr);
            writer.beginObject();
            writer.name("timeStamp").value(new Date().toString());
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
