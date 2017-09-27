/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.util;

import org.opendaylight.infrautils.diagstatus.ClusterMemberInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusShellUtil provides utilities on accessing the ODL cluster nodes.
 * This also does diagstatus for the various registered services in the cluster.
 *
 * @author Faseela K
 */
public class DiagStatusShellUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusShellUtil.class);

    public static String getNodeSpecificStatus(String ipAddress) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        if (ClusterMemberInfoProvider.isValidIPAddress(ipAddress)) {
            if (ClusterMemberInfoProvider.isIPAddressInCluster(ipAddress)) {
                if (ClusterMemberInfoProvider.isLocalIPAddress(ipAddress)) {
                    // Local IP Address
                    strBuilder.append(StatusOperations.getLocalStatusSummary(ipAddress));
                } else {
                    // Remote IP
                    strBuilder.append(StatusOperations.getRemoteStatusSummary(ipAddress));
                }
            } else {
                strBuilder.append("Invalid IP Address or Not a cluster member IP Address");
            }
        } else {
            strBuilder.append("Invalid or Empty IP Address");
        }
        return strBuilder.toString();
    }
}
