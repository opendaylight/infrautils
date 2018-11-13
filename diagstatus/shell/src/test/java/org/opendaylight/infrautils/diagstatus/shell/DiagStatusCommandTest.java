/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.net.InetAddresses;
import com.google.common.truth.BooleanSubject;
import java.net.InetAddress;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor.Behaviour;

/**
 * HttpTest for {@link DiagStatusCommand}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusCommandTest {

    static DiagStatusService diagStatusService = mock(DiagStatusService.class);
    static SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);
    static DiagStatusCommand diagStatusCommand;
    static ClusterMemberInfo clusterMemberInfo = mock(ClusterMemberInfo.class);

    static String serviceStatusSummary = "{\n"
            + "  \"timeStamp\": \"Thu Nov 15 16:41:36 IST 2018\",\n"
            + "  \"isOperational\": false,\n"
            + "  \"systemReadyState\": \"ACTIVE\",\n"
            + "  \"systemReadyStatsErrorCause\": \"\",\n"
            + "  \"statusSummary\": [\n"
            + "    {\n"
            + "      \"serviceName\": \"testService\",\n"
            + "      \"effectiveStatus\": \"STARTING\",\n"
            + "      \"reportedStatusDescription\": \"INITIALIZING\",\n"
            + "      \"statusTimeStamp\": {\n"
            + "        \"seconds\": 1542280296,\n"
            + "        \"nanos\": 678000000\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    @BeforeClass
    public static void start() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus(200);
        httpResponse.setEntity(serviceStatusSummary);
        when(httpClient.sendRequest(any())).thenReturn(httpResponse);
        diagStatusCommand = new DiagStatusCommand(new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor),
                clusterMemberInfo, httpClient);
        diagStatusCommand.setHttpClient(httpClient);
    }

    @Test
    // INFRAUTILS-56 mentions this test was previously ignored as the shutdown had issues due to the RMI connector
    // Hence trying to re-enable this as part of INFRAUTILS-45
    public void testGetRemoteStatusSummary_IPv4() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("127.0.0.1"));
    }

    @Test
    public void testGetRemoteStatusSummary_IPv6() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("::1"));
    }

    private static BooleanSubject checkGetRemoteStatusSummary(InetAddress inetAddress) throws Exception {
        String statusSummary = diagStatusCommand.getRemoteStatusSummary(inetAddress);
        return assertThat(statusSummary.contains(inetAddress.toString()));
    }
}
