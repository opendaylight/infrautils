/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.ready;

/**
 * Service which exposes system ready-ness state as MBean.
 *
 * @author Faseela K
 */
public interface SystemReadyMonitorMXBean {

    /**
     * Obtain current system status as one of few possible enumerated values.
     */
    SystemState getSystemState();

    /**
     * Obtain the cause of a {@link SystemState#FAILURE}.
     */
    String getFailureCause();
}
