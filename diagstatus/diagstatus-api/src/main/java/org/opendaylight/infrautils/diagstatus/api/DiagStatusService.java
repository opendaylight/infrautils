/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.concurrent.ThreadSafe;

/**
 * DiagStatus Service which lets users register/retrieve for particular service status details.
 *
 * @author Faseela K
 */
@ThreadSafe
public interface DiagStatusService extends AutoCloseable {

    /**
     * Register a service for status monitoring.
     *
     * @param service
     *            unique identifier for the service being registered
     *
     * @return Future useful for taking deferred action after all services are registered,
     *         or for reacting to exceptions
     */
    CompletableFuture<Void> register(String service);

    /**
     * Retrieve the status of a service specified by the identifier.
     *
     * @param serviceIdentifier
     *            unique identifier for a service
     *
     */
    Service getServiceState(String serviceIdentifier);

    /**
     * Retrieve the status of all services registered so far.
     *
     * @return  status list for all registered services
     *
     */
    List<Service> getAllServiceStates();

}
