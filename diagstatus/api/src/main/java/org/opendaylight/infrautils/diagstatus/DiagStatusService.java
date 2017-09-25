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
import org.opendaylight.infrautils.ready.SystemState;

/**
 * DiagStatus ServiceDescriptor which lets users register/retrieve for particular service status details.
 *
 * @author Faseela K
 */
@ThreadSafe
public interface DiagStatusService {

    /**
     * Register a service for status monitoring.
     *
     * @param serviceIdentifier
     *            unique identifier for the service being registered
     *
     * @return Registration status
     */
    boolean register(String serviceIdentifier);

    /**
     * Report the status of a service specified by the identifier.
     *
     * @param serviceDescriptor
     *            description of the service state
     *
     */
    void report(ServiceDescriptor serviceDescriptor);

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
     * Retrieve the system's overall state (provided by the ready service).
     */
    SystemState getSystemState();

    /**
     * Deregister a service for status monitoring.
     *
     * @param serviceIdentifier
     *            unique identifier for the service being registered
     *
     * @return Deregistration status
     */
    boolean deregister(String serviceIdentifier);
}
