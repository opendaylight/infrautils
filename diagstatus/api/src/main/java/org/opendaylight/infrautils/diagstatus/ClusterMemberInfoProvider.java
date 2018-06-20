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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public static Optional<String> getSelfAddress()  {
        Object clusterStatusMBeanValue;
        try {
            clusterStatusMBeanValue = MBeanUtils.getMBeanAttribute("akka:type=Cluster", "ClusterStatus");
        } catch (JMException e) {
            LOG.error("Problem to getMBeanAttribute(\"akka:type=Cluster\", \"ClusterStatus\"); returning empty.", e);
            return Optional.empty();
        }
        if (clusterStatusMBeanValue != null) {
            String selfAddressMbean = StringUtils.substringBetween(clusterStatusMBeanValue.toString(),
                    "\"self-address\": ", ",");
            return Optional.of(StringUtils.substringBetween(selfAddressMbean, "@", ":"));
        } else {
            LOG.error("getMBeanAttribute(\"akka:type=Cluster\", \"ClusterStatus\"); unexepected returned null");
            return Optional.empty();
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
            List<String> clusterMembers = Arrays.asList(((String)clusterMemberMBeanValue).split(","));
            for (String clusterMember : clusterMembers) {
                String nodeIp = StringUtils.substringBetween(clusterMember, "@", ":");
                clusterIPAddresses.add(nodeIp);
            }
        }
        return clusterIPAddresses;
    }

    public static boolean isValidIPAddress(String ipAddress) {
        return ipAddress != null && ipAddress.length() > 0;
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
