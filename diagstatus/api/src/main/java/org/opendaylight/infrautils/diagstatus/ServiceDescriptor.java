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

import java.time.Instant;
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

    private final String moduleServiceName;
    private final ServiceState serviceState;
    private final Instant timestamp;
    // In case of ERROR state specific error message to aid troubleshooting can be provided by monitored service:
    private final String statusDesc;
    private final @Nullable Throwable errorCause;

    private ServiceDescriptor(String moduleServiceName, ServiceState svcState, String statusDesc,
                              Throwable errorCause) {
        this.moduleServiceName = requireNonNull(moduleServiceName, "moduleServiceName");
        this.serviceState = requireNonNull(svcState, "svcState");
        this.statusDesc = requireNonNull(statusDesc, "statusDesc");
        this.timestamp = Instant.now();
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

    public Optional<Throwable> getErrorCause() {
        return Optional.ofNullable(errorCause);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this)
                .add("serviceName", getModuleServiceName())
                .add("serviceState", getServiceState())
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
        if (!Objects.equals(errorCause, other.errorCause)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleServiceName(), getServiceState(), getStatusDesc(), getTimestamp(),
                getErrorCause());
    }
}
