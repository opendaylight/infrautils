/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
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
        List<String> jmxOpArgs = new ArrayList<>();
        jmxOpArgs.add(MBeanUtils.VERBOSE_OUTPUT_FORMAT);
        String remoteJMXOperationResult = "";
        StringBuilder strBuilder = new StringBuilder();
        remoteJMXOperationResult = JMXRemoteOperationsUtil.execRemoteJMXOperation(MBeanUtils.JMX_OBJECT_NAME,
                MBeanUtils.JMX_SVCSTATUS_OPERATION_REMOTE, ipAddress, jmxOpArgs);
        strBuilder.append("Node IP Address: " + ipAddress + "\n");
        if (remoteJMXOperationResult.startsWith("ERROR")) {
            LOG.error("Error retrieving Remote Status from IP {}", ipAddress);
            strBuilder.append("Remote Status retrieval JMX Operation failed ").append(remoteJMXOperationResult);
            return strBuilder.toString();
        }
        strBuilder.append(formatJMXAsJSONStatusOutput(remoteJMXOperationResult, MBeanUtils.VERBOSE_OUTPUT_FORMAT));
        return strBuilder.toString();
    }

    private static String formatJMXAsJSONStatusOutput(String inputRawJson, String outputType) {

        StringBuilder strBuilder = new StringBuilder();
        String serviceName;
        String serviceStatus;
        if (inputRawJson.equals("{}")) {
            LOG.error("formatJMXJSONStatusOutput - Invalid inputRawJson string");
            return strBuilder.append("").toString();
        }

        // When Status summary is retrived from remote node, the JSON segment is
        // added with escape characters by Jolokia
        // So some corrections are required to parse the same as Json correctly
        // and hence additional characters are
        // suitably replaced
        if (inputRawJson.contains("javax.management.InstanceNotFoundException")) {
            return strBuilder.append("Diag Status Service Down!!").toString() + "\n";
        } else {
            JsonElement newObj = new JsonParser().parse(
                    inputRawJson.replace("\\", "").replace("\"{", "{")
                            .replace("}\"", "}")).getAsJsonObject();
            LOG.debug("formatJMXJSONStatusOutput - inputRawJson - {}",
                    inputRawJson.replace("\\", "").replace("\"{", "{")
                            .replace("}\"", "}"));
            JsonObject jsonObj = newObj.getAsJsonObject();
            JsonArray jsonArray = jsonObj.getAsJsonObject("value").getAsJsonArray("statusSummary");
            LOG.debug("formatJMXJSONStatusOutput - Retrieved json object-array of at path $.value.statusSummary");
            for (int i = 0; i < jsonArray.size(); i++) {
                if (outputType.equals(MBeanUtils.BRIEF_OUPUT_FORMAT)) {
                    strBuilder.append(jsonArray.get(i).getAsJsonObject().get("statusBrief").toString() + "\n");
                } else if (outputType.equals(MBeanUtils.DEBUG_OUTPUT_FORMAT)) {
                    serviceName = jsonArray.get(i).getAsJsonObject().get("serviceName").toString()
                            .replaceAll("\"", "");
                    serviceStatus = jsonArray.get(i).getAsJsonObject().get("effectiveStatus").toString()
                            .replaceAll("\"", "");
                    strBuilder.append("  ").append(String.format("%-20s%-20s", serviceName, ": " + serviceStatus))
                            .append("\n");
                } else {
                    serviceName = jsonArray.get(i).getAsJsonObject().get("serviceName").toString()
                            .replaceAll("\"", "");
                    serviceStatus = jsonArray.get(i).getAsJsonObject().get("effectiveStatus").toString()
                            .replaceAll("\"", "");
                    strBuilder.append("  ").append(String.format("%-20s%-20s", serviceName, ": " + serviceStatus))
                            .append("\n");
                }
            }
        }
        return strBuilder.toString();
    }
}
