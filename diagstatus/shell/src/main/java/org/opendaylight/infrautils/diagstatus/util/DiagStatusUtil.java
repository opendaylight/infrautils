/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusUtil provides utilities on accessing the ODL cluster nodes.
 * This also does diagstatus for the various registered services in the cluster.
 *
 * @author Faseela K
 */
public class DiagStatusUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusUtil.class);

    public static List<String> getClusterMembers()  {
        List<String> clusterIPAddresses = new ArrayList<>();
        Object clusterMemberMBeanValue = MBeanUtils.readMBeanAttribute("akka:type=Cluster", "Members");
        if (clusterMemberMBeanValue != null) {
            List<String> clusterMembers = Arrays.asList(((String)clusterMemberMBeanValue).split(","));
            for (String clusterMember : clusterMembers) {
                String nodeIp = StringUtils.substringBetween(clusterMember, "@", ":");
                clusterIPAddresses.add(nodeIp);
            }
        }
        return clusterIPAddresses;
    }

    public static String getNodeSpecificStatus(String ipAddress) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        if (isValidIPAddress(ipAddress)) {
            if (isIPAddressInCluster(ipAddress)) {
                if (isLocalIPAddress(ipAddress)) {
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

    static boolean isValidIPAddress(String ipAddress) {
        return (ipAddress != null && ipAddress.length() > 0);
    }

    static boolean isIPAddressInCluster(String ipAddress) {
        List<String> clusterIPAddresses = getClusterMembers();
        if (!clusterIPAddresses.contains(ipAddress)) {
            LOG.error("specified ip {} is not present in cluster", ipAddress);
            return false;
        }
        return true;
    }

    static boolean isLocalIPAddress(String ipAddress) {
        return ipAddress.equals(InetAddress.getLoopbackAddress().getHostAddress());
    }
}