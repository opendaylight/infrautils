/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Details of a registered service.
 *
 * @author Faseela K
 */
public class ServiceDescriptor {

    private final String moduleServiceName;
    private final ServiceState serviceState;
    private final Instant timestamp;
    // In case of ERROR state specific error message to aid troubleshooting can be provided by monitored service:
    private final String statusDesc;

    public ServiceDescriptor(String moduleServiceName, ServiceState svcState, String statusDesc) {
        this.moduleServiceName = requireNonNull(moduleServiceName, "moduleServiceName");
        this.serviceState = requireNonNull(svcState, "svcState");
        this.statusDesc = requireNonNull(statusDesc, "statusDesc");
        this.timestamp = Instant.now();
    }

    public String getModuleServiceName() {
        return moduleServiceName;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("serviceName", getModuleServiceName())
                .add("serviceState", getServiceState()).add("statusDesc", getStatusDesc())
                .add("timeStamp", getTimestamp()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ServiceDescriptor)) {
            return false;
        }
        ServiceDescriptor other = (ServiceDescriptor) obj;
        if (!moduleServiceName.equals(other.moduleServiceName)) {
            return false;
        }
        if (serviceState != other.serviceState) {
            return false;
        }
        if (!statusDesc.equals(other.statusDesc)) {
            return false;
        }
        if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleServiceName(), getServiceState(), getStatusDesc(), getTimestamp());
    }
}
