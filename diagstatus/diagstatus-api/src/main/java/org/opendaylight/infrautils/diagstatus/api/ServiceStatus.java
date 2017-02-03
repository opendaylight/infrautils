/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.api;

import java.util.Date;

/**
 * ServiceState for {@link ServiceStatus}.
 *
 * @author Faseela K
 */
public class ServiceStatus {

    private String moduleServiceName;
    private ServiceState serviceState;
    private Date timestamp;
    private String statusDesc; // In case of ERROR state specific error message to aid troubleshooting can be
                               // provided  by monitored service

    public ServiceStatus(String moduleServiceName, ServiceState svcState, String statusDesc) {
        this.moduleServiceName = moduleServiceName;
        this.serviceState = svcState;
        this.statusDesc = statusDesc;
        this.timestamp = new Date();
    }

    public String getModuleServiceName() {
        return moduleServiceName;
    }

    public void setModuleServiceName(String moduleServiceName) {
        this.moduleServiceName = moduleServiceName;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

}
