/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

import org.opendaylight.infrautils.ready.SystemState;

/**
 * Summary of overall system service status.
 *
 * @author Faseela K
 */
public final class ServiceStatusSummary {

    private final Instant timeStamp;
    private final boolean isOperational;
    private final SystemState systemReadyState;
    private final String systemReadyStateErrorCause;
    private final Collection<ServiceDescriptor> statusSummary;

    public ServiceStatusSummary(boolean isOperational, SystemState systemState, String systemReadyErrorCause,
                                Collection<ServiceDescriptor> statusSummary) {
        this.timeStamp = Instant.now();
        this.isOperational = isOperational;
        this.systemReadyState = systemState;
        this.statusSummary = statusSummary;
        this.systemReadyStateErrorCause = systemReadyErrorCause;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public SystemState getSystemReadyState() {
        return systemReadyState;
    }

    public String getSystemReadyStateErrorCause() {
        return systemReadyStateErrorCause;
    }

    public Collection<ServiceDescriptor> getStatusSummary() {
        return statusSummary;
    }

    public Instant getTimeStamp() {
        return timeStamp;
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

        return this.isOperational == that.isOperational
                && Objects.equals(this.systemReadyState, that.systemReadyState)
                && Objects.equals(this.systemReadyStateErrorCause, that.systemReadyStateErrorCause)
                && Objects.equals(this.statusSummary, that.statusSummary)
                && Objects.equals(this.timeStamp, that.timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOperational(), getSystemReadyState(), getSystemReadyStateErrorCause(),
                getStatusSummary(), getTimeStamp());
    }

    @Override
    public String toString() {
        return "ServiceStatusSummary{"
                + "timeStamp='" + timeStamp.toString() + '\''
                + ", isOperational=" + isOperational
                + ", systemReadyState=" + systemReadyState
                + ", systemReadyStateErrorCause='" + systemReadyStateErrorCause + '\''
                + ", statusSummary=" + statusSummary
                + '}';
    }
}