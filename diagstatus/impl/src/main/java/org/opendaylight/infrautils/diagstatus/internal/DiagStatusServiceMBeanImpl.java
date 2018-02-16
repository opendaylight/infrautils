/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static org.opendaylight.infrautils.diagstatus.ServiceState.ERROR;
import static org.opendaylight.infrautils.diagstatus.ServiceState.UNREGISTERED;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;

import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfoProvider;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.ops4j.pax.cdi.api.OsgiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DiagStatusServiceMBeanImpl extends StandardMBean implements DiagStatusServiceMBean, SystemReadyListener {

    private static final String DEBUG_OUTPUT_FORMAT = "D";
    // private static final String BRIEF_OUTPUT_FORMAT = "B";
    private static final String VERBOSE_OUTPUT_FORMAT = "V";
    private static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceMBeanImpl.class);

    private final DiagStatusService diagStatusService;
    private final SystemReadyMonitor systemReadyMonitor;
    private final MBeanServer mbeanServer;
    private Pair<JMXConnectorServer, Registry> jmxConnector = null;

    @Inject
    public DiagStatusServiceMBeanImpl(DiagStatusService diagStatusService,
                                      @OsgiService SystemReadyMonitor systemReadyMonitor)
            throws JMException, IOException {
        super(DiagStatusServiceMBean.class);
        this.diagStatusService = diagStatusService;
        this.systemReadyMonitor = systemReadyMonitor;
        systemReadyMonitor.registerListener(this);
        mbeanServer = MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
    }

    @Override
    public void onSystemBootReady() {
        ClusterMemberInfoProvider.getSelfAddress().ifPresent(host -> {
            try {
                jmxConnector = MBeanUtils.startRMIConnectorServer(mbeanServer, host);
            } catch (IOException e) {
                LOG.error("unable to start jmx connector for host {}", host);
            }
        });
    }

    @PreDestroy
    public void close() throws IOException, MalformedObjectNameException,
            InstanceNotFoundException, MBeanRegistrationException {
        MBeanUtils.unregisterServerMBean(this, JMX_OBJECT_NAME);
        if (jmxConnector != null) {
            MBeanUtils.stopRMIConnectorServer(jmxConnector);
        }
    }

    @Override
    public String acquireServiceStatus() {
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
            statusSummary.append("ServiceName          : ").append(status.getModuleServiceName()).append("\n");
            if (status.getServiceState() != null) {
                statusSummary.append("Last Reported Status : ")
                        .append(status.getServiceState().name()).append("\n");
            }
            if (status.getStatusDesc() != null) {
                statusSummary.append("Reported Status Desc : ").append(status.getStatusDesc())
                        .append("\n");
            }
            if (status.getTimestamp() != null) {
                statusSummary.append("Status Timestamp     : ").append(status.getTimestamp().toString()).append("\n\n");
            }
        }
        statusSummary.append("\n");

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusDetailed() {
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
            statusSummary
                    .append("  ")
                    .append(String.format("%-20s%-20s", status.getModuleServiceName(), ": "
                            + status.getServiceState()))
                    .append("\n");
        }
        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusBrief() {
        String errorState = "ERROR - ";
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor stat : diagStatusService.getAllServiceDescriptors()) {
            ServiceState state = stat.getServiceState();
            if (state.equals(ERROR) || state.equals(UNREGISTERED)) {
                statusSummary.append(errorState).append(stat.getModuleServiceName()).append(" ");
            }
        }
        return statusSummary.toString();
    }

    @Override
    public Map<String, String> acquireServiceStatusMap() {
        Builder<String, String> mapBuilder = ImmutableMap.builder();
        for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
            ServiceState state = status.getServiceState();
            if (state == null || state.equals(ServiceState.ERROR) || state.equals(ServiceState.UNREGISTERED)) {
                mapBuilder.put(status.getModuleServiceName(), "ERROR");
            } else {
                mapBuilder.put(status.getModuleServiceName(), "OPERATIONAL");
            }
        }
        return mapBuilder.build();
    }

    @Override
    public String acquireServiceStatusAsJSON(String formatType) {
        try {
            StringWriter strWrtr = new StringWriter();
            JsonWriter writer = new JsonWriter(strWrtr);
            writer.beginObject();
            writer.name("timeStamp").value(new Date().toString());
            writer.name("systemReadyState").value(systemReadyMonitor.getSystemState().name());
            writer.name("statusSummary");
            writer.beginArray(); //[
            for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
                writer.beginObject(); // {
                if (formatType.equals(DEBUG_OUTPUT_FORMAT)) {
                    writer.name("serviceName").value(status.getModuleServiceName());
                    writer.name("lastReportedStatus").value(status.getServiceState().name());
                    writer.name("effectiveStatus").value(status.getServiceState().name());
                    writer.name("reportedStatusDes").value(status.getStatusDesc());
                    writer.name("statusTimestamp").value(status.getTimestamp().toString());
                } else if (formatType.equals(VERBOSE_OUTPUT_FORMAT)) {
                    writer.name("serviceName").value(status.getModuleServiceName());
                    writer.name("effectiveStatus").value(status.getServiceState().name());
                } else {
                    writer.name("statusBrief").value(acquireServiceStatusBrief());
                }
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.flush();
            writer.close();
            return strWrtr.getBuffer().toString();
        } catch (IOException e) {
            LOG.error("Error while converting service status to JSON", e);
            return "{}";
        }
    }
}
