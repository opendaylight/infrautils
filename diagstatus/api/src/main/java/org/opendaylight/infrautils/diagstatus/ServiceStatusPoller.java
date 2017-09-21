/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

/**
 * Apps can implement this interface if they want to expose a live status.
 *
 * @author Faseela K
 */
public interface ServiceStatusPoller {

    /**
     * Called back whenever a northbound query comes for the current status of
     * the registered services.
     */
    ServiceDescriptor getServiceDescriptor();
}
