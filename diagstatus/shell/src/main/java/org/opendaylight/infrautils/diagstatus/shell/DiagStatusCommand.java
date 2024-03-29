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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
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

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);
    private static final String DIAGSTATUS_URL_PREFIX = "http://";

    @VisibleForTesting
    static final String DIAGSTATUS_URL_SUFFIX = "diagstatus";
    @VisibleForTesting
    static final char DIAGSTATUS_URL_SEPARATOR = '/';

    @Reference
    @VisibleForTesting
    DiagStatusServiceMBean diagStatusServiceMBean;
    @Reference
    @VisibleForTesting
    ClusterMemberInfo clusterMemberInfoProvider;
    @Reference
    @VisibleForTesting
    HttpClientService httpClient;

    @Option(name = "-n", aliases = {"--node"})
    String nip;

    @Override
    @SuppressWarnings({ "checkstyle:IllegalCatch", "checkstyle:RegexpSinglelineJava" })
    public @Nullable Object execute() throws Exception {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Timestamp: ").append(new Date().toString()).append("\n");

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

        System.out.println(strBuilder.toString());
        return null;
    }

    @VisibleForTesting
    String getLocalStatusSummary(InetAddress memberAddress) {
        return "Node IP Address: " + memberAddress.getHostAddress() + "\n"
                + diagStatusServiceMBean.acquireServiceStatusDetailed();
    }

    @VisibleForTesting
    String getRemoteStatusSummary(InetAddress memberAddress) throws Exception {
        return new StringBuilder()
            .append("Node IP Address: ").append(memberAddress.getHostAddress()).append("\n")
            .append(invokeRemoteDiagStatus(memberAddress))
            .toString();
    }

    String invokeRemoteDiagStatus(InetAddress host) throws Exception {
        String restUrl = buildRemoteDiagStatusUrl(host);
        LOG.info("invokeRemoteDiagStatus() REST URL: {}", restUrl);
        HttpRequest request = HttpRequest.newBuilder(new URI(restUrl))
                .GET()
                .timeout(HTTP_TIMEOUT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        LOG.debug("sending http request for accessing remote diagstatus");
        HttpResponse<String> response = httpClient.sendRequest(request);
        // Response code for success should be 200
        int httpResponseCode = response.statusCode();
        LOG.debug("http response received for remote diagstatus {}", httpResponseCode);
        String respStr = response.body();
        if (httpResponseCode > 299) {
            LOG.error("Non-200 http response code received {} for URL {}", httpResponseCode, restUrl);
            if (respStr == null) {
                return "Service Status Retrieval failed. HTTP Response Code : " + httpResponseCode + "\n";
            }
        }
        LOG.trace("HTTP Response is - {} for URL {}", respStr, restUrl);
        return buildServiceStatusSummaryString(ServiceStatusSummary.fromJSON(respStr));
    }

    private static String buildServiceStatusSummaryString(ServiceStatusSummary serviceStatusSummary) {
        StringBuilder statusSummary = new StringBuilder()
            .append("System is operational: ").append(serviceStatusSummary.isOperational()).append('\n')
            .append("System ready state: ").append(serviceStatusSummary.getSystemReadyState()).append('\n');
        for (ServiceDescriptor status : serviceStatusSummary.getStatusSummary()) {
            statusSummary
                    .append("  ")
                    // the magic is the max String length of ServiceState enum values, plus padding
                    .append(String.format("%-20s%-15s", status.getModuleServiceName(), ": "
                            + status.getServiceState()));
            if (!Strings.isNullOrEmpty(status.getStatusDesc())) {
                statusSummary.append(" (").append(status.getStatusDesc()).append(')');
            }
            // intentionally using Throwable.toString() instead of Throwables.getStackTraceAsString to keep CLI brief
            status.getErrorCause().ifPresent(cause -> statusSummary.append(cause.toString()));
            statusSummary.append("\n");
        }
        return statusSummary.toString();
    }

    @VisibleForTesting
    String buildRemoteDiagStatusUrl(InetAddress host) {
        String targetHostAsString;
        if (host instanceof Inet6Address) {
            targetHostAsString = '[' + host.getHostAddress() + ']';
        } else {
            targetHostAsString = host.getHostAddress();
        }
        return new StringBuilder().append(DIAGSTATUS_URL_PREFIX + targetHostAsString + ":" + httpClient.getHttpPort()
                + DIAGSTATUS_URL_SEPARATOR + DIAGSTATUS_URL_SUFFIX + DIAGSTATUS_URL_SEPARATOR).toString();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private String getNodeSpecificStatus(InetAddress ipAddress) {
        StringBuilder strBuilder = new StringBuilder();
        if (isIPAddressInCluster(ipAddress)) {
            InetAddress selfAddress = clusterMemberInfoProvider.getSelfAddress();
            if (ipAddress.equals(selfAddress)) {
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
