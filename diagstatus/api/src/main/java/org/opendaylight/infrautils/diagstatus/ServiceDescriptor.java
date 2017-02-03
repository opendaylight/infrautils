/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;
import org.opendaylight.yangtools.util.EvenMoreObjects;

/**
 * Details of a registered service.
 *
 * @author Faseela K
 */
public class ServiceDescriptor {

    private final String moduleServiceName;
    private final ServiceState serviceState;
    private final Date timestamp;
    private final String statusDesc; // In case of ERROR state specific error message to aid troubleshooting can be
                                     // provided  by monitored service

    public ServiceDescriptor(String moduleServiceName, ServiceState svcState, String statusDesc) {
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
    public String toString() {
        return MoreObjects.toStringHelper(this).add("serviceName", getModuleServiceName()).add("serviceState",
            getServiceState()).add("statusDesc", getStatusDesc()).add("timeStamp", getTimestamp()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EvenMoreObjects.equalsHelper(this, obj,
            (self, other) -> Objects.equals(self.getModuleServiceName(), other.getModuleServiceName())
                && Objects.equals(self.getServiceState(), other.getServiceState())
                && Objects.equals(self.getStatusDesc(), other.getStatusDesc())
                && Objects.equals(self.getTimestamp(), other.getTimestamp()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleServiceName(), getServiceState(), getStatusDesc(), getTimestamp());
    }
}
