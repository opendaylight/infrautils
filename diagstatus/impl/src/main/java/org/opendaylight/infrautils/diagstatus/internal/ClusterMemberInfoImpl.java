/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import static java.util.Objects.requireNonNull;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import javax.management.JMException;
import org.apache.aries.blueprint.annotation.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ClusterMemberInfo} service using the JMX MBeans
 * exposed by Akka framework used in ODL controller.
 *
 * @author Faseela K
 * @author Michael Vorburger converted former static utility to service (for testability)
 */
@Singleton
@Service(classes = ClusterMemberInfo.class)
public class ClusterMemberInfoImpl implements ClusterMemberInfo {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterMemberInfoImpl.class);

    @Override
    public InetAddress getSelfAddress() {
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
            return InetAddresses.forString(extractAddressFromAkka(selfAddressMbean));
        } else {
            throw new IllegalStateException("getMBeanAttribute(\"akka:type=Cluster\", \"ClusterStatus\") == null?!");
        }
    }

    @Override
    public List<InetAddress> getClusterMembers() {
        Object clusterMemberMBeanValue;
        try {
            clusterMemberMBeanValue = MBeanUtils.getMBeanAttribute("akka:type=Cluster", "Members");
        } catch (JMException e) {
            LOG.error("Problem to getMBeanAttribute(\"akka:type=Cluster\", \"Members\"); returning empty List", e);
            return Collections.emptyList();
        }

        List<InetAddress> clusterIPAddresses = new ArrayList<>();
        if (clusterMemberMBeanValue != null) {
            String[] clusterMembers = ((String)clusterMemberMBeanValue).split(",", -1);
            for (String clusterMember : clusterMembers) {
                String nodeIp = extractAddressFromAkka(clusterMember);
                clusterIPAddresses.add(InetAddresses.forString(nodeIp));
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
}
