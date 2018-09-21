/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import static java.util.Objects.requireNonNull;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.JMException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utilities to derive ODL cluster information.
 *
 * <p>It is currently implemented using some of the JMX MBeans exposed by Akka framework used in ODL controller.
 *
 * @author Faseela K
 */
public final class ClusterMemberInfoProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterMemberInfoProvider.class);

    private ClusterMemberInfoProvider() { }

    public static String getSelfAddress() {
        Object clusterStatusMBeanValue;
        try {
            clusterStatusMBeanValue = MBeanUtils.getMBeanAttribute("akka:type=Cluster", "ClusterStatus");
        } catch (JMException e) {
            throw new IllegalStateException("getMBeanAttribute(\"akka:type=Cluster\", \"ClusterStatus\") failed", e);
        }
        if (clusterStatusMBeanValue != null) {
            String clusterStatusText = clusterStatusMBeanValue.toString();
            String selfAddressMbean = requireNonNull(StringUtils.substringBetween(clusterStatusText,
                    "\"self-address\": ", ","), "null substringBetween() for: " + clusterStatusText);
            return extractAddressFromAkka(selfAddressMbean);
        } else {
            throw new IllegalStateException("getMBeanAttribute(\"akka:type=Cluster\", \"ClusterStatus\") == null?!");
        }
    }

    public static List<String> getClusterMembers()  {
        Object clusterMemberMBeanValue;
        try {
            clusterMemberMBeanValue = MBeanUtils.getMBeanAttribute("akka:type=Cluster", "Members");
        } catch (JMException e) {
            LOG.error("Problem to getMBeanAttribute(\"akka:type=Cluster\", \"Members\"); returning empty List", e);
            return Collections.emptyList();
        }

        List<String> clusterIPAddresses = new ArrayList<>();
        if (clusterMemberMBeanValue != null) {
            String[] clusterMembers = ((String)clusterMemberMBeanValue).split(",", -1);
            for (String clusterMember : clusterMembers) {
                String nodeIp = extractAddressFromAkka(clusterMember);
                clusterIPAddresses.add(nodeIp);
            }
        }
        return clusterIPAddresses;
    }

    private static String extractAddressFromAkka(String clusterMember) {
        if (clusterMember.contains("@[")) {
            // IPv6 address
            return requireNonNull(StringUtils.substringBetween(clusterMember, "@[", "]"),
                    "null substringBetween() for IPv6: " + clusterMember);
        }
        // IPv4 or hostname
        return requireNonNull(StringUtils.substringBetween(clusterMember, "@", ":"),
                "null substringBetween() for IPv4: " + clusterMember);
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
