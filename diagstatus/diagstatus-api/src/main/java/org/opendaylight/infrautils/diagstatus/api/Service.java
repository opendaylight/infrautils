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
 * Service details for {@link Service}.
 *
 * @author Faseela K
 */
public class Service {

    private String moduleServiceName;
    private ServiceState serviceState;
    private Date timestamp;
    private String statusDesc; // In case of ERROR state specific error message to aid troubleshooting can be
                               // provided  by monitored service

    public Service(String moduleServiceName, ServiceState svcState, String statusDesc) {
        this.moduleServiceName = moduleServiceName;
        this.serviceState = svcState;
        this.statusDesc = statusDesc;
        this.timestamp = new Date();
    }

    public String getModuleServiceName() {
        return moduleServiceName;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Service)) {
            return false;
        }

        Service service = (Service) obj;

        if (getModuleServiceName() != null ? !getModuleServiceName().equals(service.getModuleServiceName()) :
            service.getModuleServiceName() != null) {
            return false;
        }
        if (getServiceState() != service.getServiceState()) {
            return false;
        }
        if (getTimestamp() != null ? !getTimestamp().equals(service.getTimestamp()) : service.getTimestamp() != null) {
            return false;
        }
        return getStatusDesc() != null ? getStatusDesc().equals(service.getStatusDesc()) :
            service.getStatusDesc() == null;

    }

    @Override
    public int hashCode() {
        int result = getModuleServiceName() != null ? getModuleServiceName().hashCode() : 0;
        result = 31 * result + (getServiceState() != null ? getServiceState().hashCode() : 0);
        result = 31 * result + (getTimestamp() != null ? getTimestamp().hashCode() : 0);
        result = 31 * result + (getStatusDesc() != null ? getStatusDesc().hashCode() : 0);
        return result;
    }
}
