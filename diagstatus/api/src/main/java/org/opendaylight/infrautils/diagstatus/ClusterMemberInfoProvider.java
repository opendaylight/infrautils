/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *   This class provides utilities to derive ODL cluster information using some of the MBeans exposed by
 *   ODL akka framework.
 *
 *  @author Faseela K
 */
public class ClusterMemberInfoProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterMemberInfoProvider.class);

    public static String getSelfAddress()  {
        Object clusterStatusMBeanValue = MBeanUtils.readMBeanAttribute("akka:type=Cluster", "ClusterStatus");
        if (clusterStatusMBeanValue != null) {
            String selfAddressMbean = StringUtils.substringBetween(clusterStatusMBeanValue.toString(),
                    "\"self-address\": ", ",");
            return StringUtils.substringBetween(selfAddressMbean, "@", ":");
        }
        return null;
    }

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

    public static boolean isValidIPAddress(String ipAddress) {
        return (ipAddress != null && ipAddress.length() > 0);
    }

    public static boolean isIPAddressInCluster(String ipAddress) {
        List<String> clusterIPAddresses = getClusterMembers();
        if (!clusterIPAddresses.contains(ipAddress)) {
            LOG.error("specified ip {} is not present in cluster", ipAddress);
            return false;
        }
        return true;
    }

    public static boolean isLocalIPAddress(String ipAddress) {
        return ipAddress.equals(InetAddress.getLoopbackAddress().getHostAddress());
    }
}
