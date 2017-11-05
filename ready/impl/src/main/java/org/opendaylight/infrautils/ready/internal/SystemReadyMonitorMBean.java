/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.ready.internal;

/**
 * Service which exposes system ready-ness state as MBean
 *
 * @author Faseela K
 */
public interface SystemReadyMonitorMBean {
    /**
     * Returns the overall state of the system based on the status of bundle bringup for the features.
     *
     * @return SystemState Current state of the system
     */
    String getCurrentSystemState();
}
