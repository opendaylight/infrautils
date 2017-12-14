/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfoProvider;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI for showing registered service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "showSvcStatus", description = "show the status of registered services")
public class DiagStatusCommand implements org.apache.karaf.shell.commands.Action {
    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    @Option(name = "-n", aliases = {"--node"})
    String nip;

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public Object execute(CommandSession session) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Timestamp: " + new java.util.Date().toString() + "\n");

        if (null != nip) {
            strBuilder.append(getNodeSpecificStatus(nip));
        } else {
            List<String> clusterIPAddresses = ClusterMemberInfoProvider.getClusterMembers();
            if (!clusterIPAddresses.isEmpty()) {
                for (String remoteIpAddr : clusterIPAddresses) {
                    try {
                        strBuilder.append(getRemoteStatusSummary(remoteIpAddr));
                    } catch (Exception e) {
                        strBuilder.append("Remote Status retrieval JMX Operation failed for node ")
                                .append(remoteIpAddr);
                        LOG.error("Exception while reaching Host {}", remoteIpAddr, e);
                    }
                }
            } else {
                LOG.info("Could not obtain cluster members or the cluster-command is being executed locally\n");
                strBuilder.append(getLocalStatusSummary("localhost"));
            }
        }

        session.getConsole().print(strBuilder.toString());
        return null;
    }

    public static String getLocalStatusSummary(String localIPAddress) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Node IP Address: " + localIPAddress + "\n");
        strBuilder.append(MBeanUtils.invokeMBeanFunction(MBeanUtils.JMX_OBJECT_NAME,
                MBeanUtils.JMX_SVCSTATUS_OPERATION_DETAILED));
        return strBuilder.toString();
    }

    public static String getRemoteStatusSummary(String ipAddress) throws Exception {
        String remoteJMXOperationResult;
        StringBuilder strBuilder = new StringBuilder();
        remoteJMXOperationResult = MBeanUtils.invokeRemoteJMXOperation(ipAddress, MBeanUtils.JMX_OBJECT_NAME);
        strBuilder.append("Node IP Address: " + ipAddress + "\n");
        strBuilder.append(remoteJMXOperationResult);
        return strBuilder.toString();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public static String getNodeSpecificStatus(String ipAddress) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        if (ClusterMemberInfoProvider.isValidIPAddress(ipAddress)) {
            if (ClusterMemberInfoProvider.isIPAddressInCluster(ipAddress)) {
                if (ClusterMemberInfoProvider.isLocalIPAddress(ipAddress)) {
                    // Local IP Address
                    strBuilder.append(getLocalStatusSummary(ipAddress));
                } else {
                    // Remote IP
                    try {
                        strBuilder.append(getRemoteStatusSummary(ipAddress));
                    } catch (Exception e) {
                        strBuilder.append("Remote Status retrieval JMX Operation failed for node ").append(ipAddress);
                        LOG.error("Exception while reaching Host ::{}", ipAddress);
                    }
                }
            } else {
                strBuilder.append("Invalid IP Address or Not a cluster member IP Address ").append(ipAddress);
            }
        } else {
            strBuilder.append("Invalid or Empty IP Address");
        }
        return strBuilder.toString();
    }
}