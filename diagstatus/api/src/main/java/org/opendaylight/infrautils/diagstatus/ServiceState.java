/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

/**
 * ServiceState for {@link ServiceState}.
 *
 * @author Faseela K
 */
public enum ServiceState {

    UNREGISTERED,   // This state is used only by the monitor implementation and not by monitored services.
    STARTING,       // move to this state when registration call is received from monitored service
    OPERATIONAL,    // This state is used only by the monitored services for reactive and proactive reporting.
    SUSPECTED,      // This state is used only by the monitor implementation and not by monitored services.
    RECOVERED,      // This state is used only by the monitor implementation and not by monitored services.
    ERROR;          // This state is used only by the monitored services for reactive and proactive reporting.
}
