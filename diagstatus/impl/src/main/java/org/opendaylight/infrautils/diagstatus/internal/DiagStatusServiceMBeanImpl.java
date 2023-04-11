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

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.StandardMBean;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(immediate = true, service = DiagStatusServiceMBean.class)
// In case you are wondering: yes, this is published to OSGi registry for the sake of diagstatus-shell. Not the grandest
// of ideas but it works for now.
public class DiagStatusServiceMBeanImpl extends StandardMBean implements DiagStatusServiceMBean, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceMBeanImpl.class);

    private final DiagStatusService diagStatusService;
    private final SystemReadyMonitor systemReadyMonitor;

    @Inject
    @Activate
    public DiagStatusServiceMBeanImpl(@Reference DiagStatusService diagStatusService,
            @Reference SystemReadyMonitor systemReadyMonitor) throws JMException {
        super(DiagStatusServiceMBean.class);
        this.diagStatusService = diagStatusService;
        this.systemReadyMonitor = systemReadyMonitor;
        MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
        LOG.info("Diagnostic Status Service management started");
    }

    @Override
    @Deactivate
    @PreDestroy
    public void close() throws InstanceNotFoundException, MBeanRegistrationException {
        MBeanUtils.unregisterServerMBean(this, JMX_OBJECT_NAME);
        LOG.info("Diagnostic Status Service management stopped");
    }

    @Override
    public String acquireServiceStatus() {
        StringBuilder statusSummary = new StringBuilder();
        ServiceStatusSummary summary = diagStatusService.getServiceStatusSummary();
        statusSummary.append("System is operational: ").append(summary.isOperational()).append('\n');
        statusSummary.append("System ready state: ").append(summary.getSystemReadyState()).append('\n');
        for (ServiceDescriptor status : summary.getStatusSummary()) {
            statusSummary.append("ServiceName          : ").append(status.getModuleServiceName()).append('\n');
            if (status.getServiceState() != null) {
                statusSummary.append("Last Reported Status : ")
                        .append(status.getServiceState().name()).append('\n');
            }
            if (status.getStatusDesc() != null) {
                statusSummary.append("Reported Status Desc : ").append(status.getStatusDesc())
                        .append('\n');
            }
            if (status.getStatusTimestamp() != null) {
                statusSummary.append("Status Timestamp     : ").append(status.getStatusTimestamp()).append("\n");
            }
            if (status.getErrorCause() != null && status.getErrorCause().isPresent()) {
                statusSummary.append("Error Cause          : ")
                        .append(Throwables.getStackTraceAsString(status.getErrorCause().orElseThrow())).append("\n");
            }
            statusSummary.append('\n');
        }
        statusSummary.append('\n');

        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusDetailed() {
        // not so detailed as acquireServiceStatus()
        StringBuilder statusSummary = new StringBuilder();
        ServiceStatusSummary summary = diagStatusService.getServiceStatusSummary();
        statusSummary.append("System is operational: ").append(summary.isOperational()).append('\n');
        statusSummary.append("System ready state: ").append(systemReadyMonitor.getSystemState()).append('\n');
        for (ServiceDescriptor status : summary.getStatusSummary()) {
            statusSummary
                    .append("  ")
                    // the magic is the max String length of ServiceState enum values, plus padding
                    .append(String.format("%-20s%-15s", status.getModuleServiceName(), ": "
                            + status.getServiceState()));
            if (!Strings.isNullOrEmpty(status.getStatusDesc())) {
                statusSummary.append(" (");
                statusSummary.append(status.getStatusDesc());
                statusSummary.append(")");
            }
            // intentionally using Throwable.toString() instead of Throwables.getStackTraceAsString to keep CLI brief
            status.getErrorCause().ifPresent(cause -> statusSummary.append(cause.toString()));
            statusSummary.append("\n");
        }
        return statusSummary.toString();
    }

    @Override
    public String acquireServiceStatusBrief() {
        String errorState = "ERROR - ";
        StringBuilder statusSummary = new StringBuilder();
        ServiceStatusSummary summary = diagStatusService.getServiceStatusSummary();
        statusSummary.append("System is operational: ").append(summary.isOperational()).append('\n');
        statusSummary.append("System ready state: ").append(summary.getSystemReadyState()).append('\n');
        for (ServiceDescriptor stat : summary.getStatusSummary()) {
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
        ServiceStatusSummary summary = diagStatusService.getServiceStatusSummary();
        for (ServiceDescriptor status : summary.getStatusSummary()) {
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
    public String acquireServiceStatusAsJSON() {
        return diagStatusService.getServiceStatusSummary().toJSON();
    }
}
