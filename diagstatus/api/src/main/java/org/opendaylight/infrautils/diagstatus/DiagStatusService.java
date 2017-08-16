/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/**
 * DiagStatus ServiceDescriptor which lets users register/retrieve for particular service status details.
 *
 * @author Faseela K
 */
@ThreadSafe
public interface DiagStatusService extends AutoCloseable {

    /**
     * Register a service for status monitoring.
     *
     * @param serviceIdentifier
     *            unique identifier for the service being registered
     *
     * @return Registration status
     */
    Boolean register(String serviceIdentifier);

    /**
     * Report the status of a service specified by the identifier.
     *
     * @param serviceIdentifier
     *            unique identifier for a service
     * @param statusDescription
     *            current status of the service
     * @param serviceState
     *            optional description if apps want to convey some details about the current state
     *
     */
    void report(String serviceIdentifier, ServiceState serviceState, String statusDescription);

    /**
     * Retrieve the status of a service specified by the identifier.
     *
     * @param serviceIdentifier
     *            unique identifier for a service
     *
     */
    ServiceDescriptor getServiceDescriptor(String serviceIdentifier);

    /**
     * Retrieve the status of all services registered so far.
     *
     * @return  status set for all registered services
     *
     */
    Collection<ServiceDescriptor> getAllServiceDescriptors();

    /**
     * Deregister a service for status monitoring.
     *
     * @param serviceIdentifier
     *            unique identifier for the service being registered
     *
     * @return Deregistration status
     */
    Boolean deregister(String serviceIdentifier);
}
