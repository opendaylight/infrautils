/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import java.util.List;
import java.util.concurrent.CompletionStage;
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
     * @return Completion Stage information for the registration
     */
    CompletionStage<Void> register(String serviceIdentifier);

    /**
     * Report the status of a service specified by the identifier.
     *
     * @param serviceIdentifier
     *            unique identifier for a service
     * @param serviceState
     *            current status of the service
     *
     */
    void report(String serviceIdentifier, ServiceState serviceState);

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
     * @return  status list for all registered services
     *
     */
    List<ServiceDescriptor> getAllServiceDescriptors();

}
