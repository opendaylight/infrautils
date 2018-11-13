/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.google.errorprone.annotations.Var;
import com.google.gson.Gson;
import java.io.PrintStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
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
    public static final int TIMEOUT = 5000;
    public static final int HTTP_PORT = 8181;
    public static final char DIAGSTATUS_URL_SEPARATOR = '/';
    public static final String DIAGSTATUS_URL_PREFIX = "http://";
    public static final String DIAGSTATUS_URL_SUFFIX = "diagstatus";

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
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Node IP Address: ").append(memberAddress).append("\n");
        strBuilder.append(invokeRemoteDiagStatus(memberAddress));
        return strBuilder.toString();
    }

    @Nullable
    static String invokeRemoteDiagStatus(InetAddress host) throws Exception {
        String restUrl = buildRemoteDiagStatusUrl(host);
        LOG.info("invokeRemoteDiagStatus() REST URL: {}", restUrl);
        HTTPRequest request = new HTTPRequest();
        request.setUri(restUrl);
        request.setMethod("GET");
        request.setTimeout(TIMEOUT);
        Map<String, List<String>> headers = new HashMap<>();
        @Var List<String> header = new ArrayList<>();
        headers.put("Authorization", header);
        header = new ArrayList<>();
        header.add("application/json");
        headers.put("Accept", header);
        request.setHeaders(headers);
        request.setContentType("application/json");
        LOG.debug("sending http request for accessing remote diagstatus");
        HTTPResponse response = HTTPClient.sendRequest(request);
        // Response code for success should be 200
        Integer httpResponseCode = response.getStatus();
        LOG.debug("http response received for remote diagstatus {}", httpResponseCode);
        String respStr = response.getEntity();
        if (httpResponseCode > 299) {
            LOG.error("Non-200 http response code received {} for URL {}", httpResponseCode, restUrl);
            return respStr + " HTTP Response Code : " + Integer.toString(httpResponseCode);
        }
        LOG.trace("HTTP Response is - {} for URL {}", respStr, restUrl);
        Gson gson = new Gson();
        return buildServiceStatusSummaryString(gson.fromJson(respStr, ServiceStatusSummary.class));
    }

    private static String buildServiceStatusSummaryString(ServiceStatusSummary serviceStatusSummary) {
        StringBuilder statusSummary = new StringBuilder();
        statusSummary.append("System is operational: ").append(serviceStatusSummary.getOperational()).append('\n');
        statusSummary.append("System ready state: ").append(serviceStatusSummary.getSystemState()).append('\n');
        for (ServiceDescriptor status : serviceStatusSummary.getServiceDescriptors()) {
            statusSummary
                    .append("  ")
                    // the magic is the max String length of ServiceState enum values, plus padding
                    .append(String.format("%-20s%-15s", status.getModuleServiceName(), ": "
                            + status.getServiceState()));
            if (!Strings.isNullOrEmpty(status.getStatusDesc())) {
                statusSummary.append(" (");
                statusSummary.append(status.getStatusDesc());
                statusSummary.append(")");
            }
            // intentionally using Throwable.toString() instead of Throwables.getStackTraceAsString to keep CLI brief
            status.getErrorCause().ifPresent(cause -> statusSummary.append(cause.toString()));
            statusSummary.append("\n");
        }
        return statusSummary.toString();
    }

    private static String buildRemoteDiagStatusUrl(InetAddress host) {
        String targetHostAsString;
        if (host instanceof Inet6Address) {
            targetHostAsString = '[' + host.getHostAddress() + ']';
        } else {
            targetHostAsString = host.getHostAddress();
        }
        return new StringBuilder().append(DIAGSTATUS_URL_PREFIX + targetHostAsString + ":" + HTTP_PORT
                + DIAGSTATUS_URL_SEPARATOR + DIAGSTATUS_URL_SUFFIX + DIAGSTATUS_URL_SEPARATOR).toString();
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
