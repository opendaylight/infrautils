/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusOperations {

    /*
     * MBeans of Local Node is accessed using standard PlatformMBeanServer
     * MBeans of remote Nodes of cluster would be accessed using REST API using
     * Jolokia as JSON-over-HHTP
     */
    private static final Logger LOG = LoggerFactory.getLogger(StatusOperations.class);

    public static String getLocalStatusSummary(String localIPAddress) {
        //TODO
        return null;
    }

    public static String getRemoteStatusSummary(String ipAddress) {
        //TODO
        return null;
    }
}
