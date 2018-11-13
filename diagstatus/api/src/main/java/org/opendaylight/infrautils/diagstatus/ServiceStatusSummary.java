/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.util.Collection;
import java.util.Objects;

import org.opendaylight.infrautils.ready.SystemState;

/**
 * Summary of overall system service status.
 *
 * @author Faseela K
 */
public final class ServiceStatusSummary {

    private final Boolean isOperational;
    private final SystemState systemState;
    private final String systemReadyErrorCause;
    private final Collection<ServiceDescriptor> serviceDescriptors;

    public ServiceStatusSummary(Boolean isOperational, SystemState systemState, String systemReadyErrorCause,
                                Collection<ServiceDescriptor> serviceDescriptors) {
        this.isOperational = isOperational;
        this.systemState = systemState;
        this.serviceDescriptors = serviceDescriptors;
        this.systemReadyErrorCause = systemReadyErrorCause;
    }

    public Boolean getOperational() {
        return isOperational;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public String getSystemReadyErrorCause() {
        return systemReadyErrorCause;
    }

    public Collection<ServiceDescriptor> getServiceDescriptors() {
        return serviceDescriptors;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServiceStatusSummary)) {
            return false;
        }

        ServiceStatusSummary that = (ServiceStatusSummary) obj;

        if (isOperational != null ? !isOperational.equals(that.isOperational) : that.isOperational != null) {
            return false;
        }
        if (getSystemState() != that.getSystemState()) {
            return false;
        }
        if (getSystemReadyErrorCause() != null ? !getSystemReadyErrorCause().equals(
                that.getSystemReadyErrorCause()) : that.getSystemReadyErrorCause() != null) {
            return false;
        }
        return getServiceDescriptors() != null ? getServiceDescriptors()
                .equals(that.getServiceDescriptors()) : that.getServiceDescriptors() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperational(), getSystemState(), getSystemReadyErrorCause(), getServiceDescriptors());
    }

    @Override
    public String toString() {
        return "ServiceStatusSummary{"
                + "isOperational=" + isOperational + ", systemState=" + systemState + ", serviceDescriptors="
                + serviceDescriptors + '}';
    }
}