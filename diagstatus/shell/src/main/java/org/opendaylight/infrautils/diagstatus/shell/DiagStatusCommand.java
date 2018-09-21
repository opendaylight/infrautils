/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import com.google.common.base.Strings;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfoProvider;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI for showing registered service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "showSvcStatus", description = "show the status of registered services")
@Service
public class DiagStatusCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    @Reference
    private DiagStatusServiceMBean diagStatusServiceMBean;

    @Option(name = "-n", aliases = {"--node"})
    String nip;

    @Override
    @Nullable
    @SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpSinglelineJava"})
    public Object execute() throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Timestamp: ").append(new java.util.Date().toString()).append("\n");

        if (null != nip) {
            strBuilder.append(getNodeSpecificStatus(nip));
        } else {
            List<String> clusterIPAddresses = ClusterMemberInfoProvider.getClusterMembers();
            if (!clusterIPAddresses.isEmpty()) {
                String selfAddress = ClusterMemberInfoProvider.getSelfAddress();
                for (String memberAddress : clusterIPAddresses) {
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
                strBuilder.append(getLocalStatusSummary("localhost"));
            }
        }

        System.out.println(strBuilder.toString());
        return null;
    }

    private String getLocalStatusSummary(String localIPAddress) {
        return "Node IP Address: " + localIPAddress + "\n" + diagStatusServiceMBean.acquireServiceStatusDetailed();
    }

    private static String getRemoteStatusSummary(String ipAddress) throws Exception {
        String url = MBeanUtils.constructJmxUrl(ipAddress, MBeanUtils.RMI_REGISTRY_PORT);
        LOG.info("invokeRemoteJMXOperation() JMX service URL: {}", url);

        LOG.info("fetching status summary for node : {}", ipAddress);
        String remoteJMXOperationResult = MBeanUtils.invokeRemoteMBeanOperation(url, MBeanUtils.JMX_OBJECT_NAME,
                DiagStatusServiceMBean.class, remoteMBean -> remoteMBean.acquireServiceStatusDetailed());

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Node IP Address: ").append(ipAddress).append("\n");
        strBuilder.append(remoteJMXOperationResult);
        return strBuilder.toString();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private String getNodeSpecificStatus(String ipAddress) throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        if (!Strings.isNullOrEmpty(ipAddress)) {
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
                        LOG.error("Exception while reaching Host: {}", ipAddress, e);
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
