/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfoProvider;
import org.opendaylight.infrautils.diagstatus.util.DiagStatusShellUtil;
import org.opendaylight.infrautils.diagstatus.util.StatusOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI for showing registered service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "showSvcStatus", description = "show the status of registered services")
public class DiagStatusCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    @Option(name = "-n", aliases = {"--node"})
    String nip;
    @Option(name = "-a", aliases = {"--all"})
    String all;

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected Object doExecute() throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Timestamp: " + new java.util.Date().toString() + "\n");

        if (null != nip) {
            strBuilder.append(DiagStatusShellUtil.getNodeSpecificStatus(nip));
        } else {
            List<String> clusterIPAddresses = ClusterMemberInfoProvider.getClusterMembers();
            if (!clusterIPAddresses.isEmpty()) {
                for (String remoteIpAddr : clusterIPAddresses) {
                    try {
                        strBuilder.append(StatusOperations.getRemoteStatusSummary(remoteIpAddr));
                    } catch (Exception e) {
                        LOG.error("Exception while reaching Host ::{}", remoteIpAddr);
                    }
                }
            } else {
                LOG.info("Could not obtain cluster members or the cluster-command is being executed locally\n");
                strBuilder.append(StatusOperations.getLocalStatusSummary("localhost"));
            }
        }

        session.getConsole().print(strBuilder.toString());
        return null;
    }
}