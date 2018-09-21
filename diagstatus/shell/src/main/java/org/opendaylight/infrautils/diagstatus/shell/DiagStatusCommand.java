/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import com.google.common.annotations.VisibleForTesting;
import java.io.PrintStream;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.List;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.opendaylight.infrautils.shell.LoggingAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI for showing registered service status.
 *
 * @author Faseela K
 */
@Service
@Command(scope = "diagstatus", name = "showSvcStatus", description = "show the status of registered services")
public class DiagStatusCommand extends LoggingAction {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    @Reference
    private DiagStatusServiceMBean diagStatusServiceMBean;

    @Reference
    private ClusterMemberInfo clusterMemberInfoProvider;

    @Option(name = "-n", aliases = {"--node"})
    String nip;

    @Override
    @SuppressWarnings({"checkstyle:IllegalCatch"})
    protected void run(PrintStream ps) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Timestamp: ").append(new java.util.Date().toString()).append("\n");

        if (null != nip) {
            strBuilder.append(getNodeSpecificStatus(InetAddresses.forString(nip)));
        } else {
            List<InetAddress> clusterIPAddresses = clusterMemberInfoProvider.getClusterMembers();
            if (!clusterIPAddresses.isEmpty()) {
                InetAddress selfAddress = clusterMemberInfoProvider.getSelfAddress();
                for (InetAddress memberAddress : clusterIPAddresses) {
                    try {
                        if (memberAddress.equals(selfAddress)) {
                            strBuilder.append(getLocalStatusSummary(memberAddress));
                        } else {
                            strBuilder.append(getRemoteStatusSummary(memberAddress));
                        }
                    } catch (Exception e) {
                        strBuilder.append("Node IP Address: ")
                                .append(memberAddress).append(" : status retrieval failed due to ")
                                .append(e.getMessage()).append("\n");
                        LOG.error("Exception while reaching Host {}", memberAddress, e);
                    }
                }
            } else {
                LOG.info("Could not obtain cluster members or the cluster-command is being executed locally\n");
                strBuilder.append(getLocalStatusSummary(InetAddress.getLoopbackAddress()));
            }
        }

        ps.println(strBuilder.toString());
    }

    private String getLocalStatusSummary(InetAddress memberAddress) {
        return "Node IP Address: " + memberAddress.toString() + "\n"
                + diagStatusServiceMBean.acquireServiceStatusDetailed();
    }

    @VisibleForTesting
    static String getRemoteStatusSummary(InetAddress memberAddress) throws Exception {
        String url = MBeanUtils.constructJmxUrl(memberAddress, MBeanUtils.RMI_REGISTRY_PORT);
        LOG.info("invokeRemoteJMXOperation() JMX service URL: {}", url);

        String remoteJMXOperationResult = MBeanUtils.invokeRemoteMBeanOperation(url, MBeanUtils.JMX_OBJECT_NAME,
                DiagStatusServiceMBean.class, remoteMBean -> remoteMBean.acquireServiceStatusDetailed());

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Node IP Address: ").append(memberAddress).append("\n");
        strBuilder.append(remoteJMXOperationResult);
        return strBuilder.toString();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private String getNodeSpecificStatus(InetAddress ipAddress) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        if (isIPAddressInCluster(ipAddress)) {
            if (clusterMemberInfoProvider.isLocalAddress(ipAddress)) {
                // Local IP Address
                strBuilder.append(getLocalStatusSummary(ipAddress));
            } else {
                // Remote IP
                try {
                    strBuilder.append(getRemoteStatusSummary(ipAddress));
                } catch (Exception e) {
                    strBuilder.append("Remote Status retrieval JMX Operation failed for node: ").append(ipAddress);
                    LOG.error("Exception while reaching Host: {}", ipAddress, e);
                }
            }
        } else {
            strBuilder.append("Invalid IP Address or Not a cluster member IP Address: ").append(ipAddress);
        }
        return strBuilder.toString();
    }

    private boolean isIPAddressInCluster(InetAddress ipAddress) {
        List<InetAddress> clusterIPAddresses = clusterMemberInfoProvider.getClusterMembers();
        if (!clusterIPAddresses.contains(ipAddress)) {
            LOG.error("specified ip {} is not present in cluster", ipAddress);
            return false;
        }
        return true;
    }
}
