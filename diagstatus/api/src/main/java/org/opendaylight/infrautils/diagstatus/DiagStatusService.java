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
 * <p>The term "service" in this context refers to a "higher-level functional service", not an OSGi Service interface.
 * (It could map to an OSGi service interface, or several of them, or none.)
 *
 * @author Faseela K
 * @author Michael Vorburger
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
    ServiceRegistration register(String serviceIdentifier);

    /**
     * Report the status of a service specified by the identifier.
     *
     * @param serviceDescriptor
     *            description of the service state
     */
    void report(ServiceDescriptor serviceDescriptor);

    /**
     * Retrieve the status of a service specified by the identifier.
     *
     * @param serviceIdentifier
     *            unique identifier for a service
     */
    ServiceDescriptor getServiceDescriptor(String serviceIdentifier);

    /**
     * Retrieve the status of all services registered so far.
     *
     * @return status set for all registered services
     */
    Collection<ServiceDescriptor> getAllServiceDescriptors();

    /**
     * Retrieve the status of all services registered so far.
     *
     * @return status as a {@link ServiceStatusSummary}
     */
    ServiceStatusSummary getServiceStatusSummary();
}
