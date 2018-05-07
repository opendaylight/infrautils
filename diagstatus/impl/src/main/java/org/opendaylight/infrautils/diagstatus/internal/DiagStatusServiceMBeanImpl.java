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
import java.io.IOException;
import java.rmi.registry.Registry;
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
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@OsgiServiceProvider(classes = DiagStatusServiceMBean.class)
public class DiagStatusServiceMBeanImpl extends StandardMBean implements DiagStatusServiceMBean, SystemReadyListener {

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
        statusSummary.append("System is operational: ").append(diagStatusService.isOperational()).append('\n');
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
            statusSummary.append("ServiceName          : ").append(status.getModuleServiceName()).append('\n');
            if (status.getServiceState() != null) {
                statusSummary.append("Last Reported Status : ")
                        .append(status.getServiceState().name()).append('\n');
            }
            if (status.getStatusDesc() != null) {
                statusSummary.append("Reported Status Desc : ").append(status.getStatusDesc())
                        .append('\n');
            }
            if (status.getTimestamp() != null) {
                statusSummary.append("Status Timestamp     : ").append(status.getTimestamp().toString()).append("\n\n");
            }
        }
        statusSummary.append('\n');

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusDetailed() {
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System is operational: ").append(diagStatusService.isOperational()).append('\n');
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor status : diagStatusService.getAllServiceDescriptors()) {
            statusSummary
                    .append("  ")
                    .append(String.format("%-20s%-20s", status.getModuleServiceName(), ": "
                            + status.getServiceState()));
            status.getErrorCause().ifPresent(cause -> statusSummary.append(cause.toString()));
            statusSummary.append("\n");
        }
        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusBrief() {
        String errorState = "ERROR - ";
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System is operational: ").append(diagStatusService.isOperational()).append('\n');
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
            if (state == null) {
                mapBuilder.put(status.getModuleServiceName(), ServiceState.UNREGISTERED.name());
            } else {
                mapBuilder.put(status.getModuleServiceName(), state.name());
            }
        }
        return mapBuilder.build();
    }

    @Override
    @Deprecated
    public String acquireServiceStatusAsJSON(String outputType) {
        return this.acquireServiceStatusAsJSON();
    }

    @Override
    public String acquireServiceStatusAsJSON() {
        return diagStatusService.getAllServiceDescriptorsAsJSON();
    }
}
