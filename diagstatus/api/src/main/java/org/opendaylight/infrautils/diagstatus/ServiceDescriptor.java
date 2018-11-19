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
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Details of a registered service.
 *
 * @author Faseela K
 */
@Immutable
@com.google.errorprone.annotations.Immutable
@SuppressWarnings("Immutable") // Throwable is not really immutable (although maybe it should have been)
public final class ServiceDescriptor {

    private final String serviceName;
    private final ServiceState effectiveStatus;
    @SerializedName("reportedStatusDescription")
    private final String statusDesc;
    private final String statusTimestamp;
    // In case of ERROR state specific error message to aid troubleshooting can be provided by monitored service:
    private final @Nullable Throwable errorCause;

    private ServiceDescriptor(String serviceName, ServiceState svcState, String statusDesc,
                              Throwable errorCause) {
        this.serviceName = requireNonNull(serviceName, "serviceName");
        this.effectiveStatus = requireNonNull(svcState, "svcState");
        this.statusDesc = requireNonNull(statusDesc, "statusDesc");
        this.statusTimestamp = new Date().toString();
        this.errorCause = errorCause;
    }

    public ServiceDescriptor(String moduleServiceName, ServiceState svcState) {
        this(moduleServiceName, svcState, "", null);
    }

    public ServiceDescriptor(String moduleServiceName, ServiceState svcState, String statusDesc) {
        this(moduleServiceName, svcState, statusDesc, null);
    }

    public ServiceDescriptor(String moduleServiceName, Throwable errorCause) {
        this(moduleServiceName, ServiceState.ERROR, "", errorCause);
    }

    public String getModuleServiceName() {
        return serviceName;
    }

    public ServiceState getServiceState() {
        return effectiveStatus;
    }

    public String getStatusTimestamp() {
        return statusTimestamp;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public Optional<Throwable> getErrorCause() {
        return Optional.ofNullable(errorCause);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this)
                .add("serviceName", getModuleServiceName())
                .add("effectiveStatus", getServiceState())
                .add("statusTimestamp", getStatusTimestamp())
                .add("statusDesc", getStatusDesc());
        getErrorCause().ifPresent(cause -> toStringHelper.add("errorCause", cause));
        return toStringHelper.toString();
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
        if (!serviceName.equals(other.serviceName)) {
            return false;
        }
        if (effectiveStatus != other.effectiveStatus) {
            return false;
        }
        if (!statusDesc.equals(other.statusDesc)) {
            return false;
        }
        if (!statusTimestamp.equals(other.statusTimestamp)) {
            return false;
        }
        if (!Objects.equals(errorCause, other.errorCause)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleServiceName(), getServiceState(), getStatusDesc(), getStatusTimestamp(),
                getErrorCause());
    }
}
