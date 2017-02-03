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

public class DiagStatusUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusUtil.class);

    public String getNodeSpecificStatus(String ipAddress) {
        // TODO
        return null;
    }

    public static boolean isValidIPAddress(String ipAddress) {
        return (ipAddress != null && ipAddress.length() > 0);
    }

    public boolean isIPAddressInCluster(String ipAddress) {
        //TODO
        return false;
    }

    public boolean isLocalIPAddress(String ipAddress) {
        //TODO
        return false;
    }

    private String getRemoteBufferSummary(String ipAddress) {
        return "TODO";
    }
}