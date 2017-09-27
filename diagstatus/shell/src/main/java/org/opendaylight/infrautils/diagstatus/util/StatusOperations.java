/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.util;

import org.opendaylight.infrautils.diagstatus.MBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusOperations {

    private static final Logger LOG = LoggerFactory.getLogger(StatusOperations.class);

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
        if (remoteJMXOperationResult.startsWith("ERROR")) {
            LOG.error("Error retrieving Remote Status from IP {}", ipAddress);
            strBuilder.append("Remote Status retrieval JMX Operation failed ").append(remoteJMXOperationResult);
            return strBuilder.toString();
        } else {
            strBuilder.append(remoteJMXOperationResult);
        }
        return strBuilder.toString();
    }
}
