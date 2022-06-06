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
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Details of a registered service.
 *
 * @author Faseela K
 */
@Immutable
@SuppressWarnings("Immutable") // Throwable is not really immutable (although maybe it should have been)
public final class ServiceDescriptor {

    private final String serviceName;
    private final ServiceState effectiveStatus;
    @SerializedName("reportedStatusDescription")
    private final String statusDesc;
    private final Instant statusTimestamp;
    // In case of ERROR state specific error message to aid troubleshooting can be provided by monitored service:
    private final @Nullable Throwable errorCause;

    private ServiceDescriptor(String serviceName, ServiceState svcState, String statusDesc,
                              Throwable errorCause) {
        this.serviceName = requireNonNull(serviceName, "serviceName");
        this.effectiveStatus = requireNonNull(svcState, "svcState");
        this.statusDesc = requireNonNull(statusDesc, "statusDesc");
        this.statusTimestamp = Instant.now();
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

    public Instant getStatusTimestamp() {
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
                .add("statusTimestamp", getStatusTimestamp().toString())
                .add("statusDesc", getStatusDesc());
        if (errorCause != null) {
            toStringHelper.add("errorCause", errorCause);
        }
        return toStringHelper.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ServiceDescriptor other && serviceName.equals(other.serviceName)
                && effectiveStatus == other.effectiveStatus && statusDesc.equals(other.statusDesc)
                && statusTimestamp.equals(other.statusTimestamp) && Objects.equals(errorCause, other.errorCause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleServiceName(), getServiceState(), getStatusDesc(), getStatusTimestamp(),
                getErrorCause());
    }
}
