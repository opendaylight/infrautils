/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.opendaylight.infrautils.diagstatus.ServiceState.OPERATIONAL;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Collections;
import javax.servlet.ServletException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceImpl;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.diagstatus.web.DiagStatusServlet;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor.Behaviour;
import org.opendaylight.infrautils.testutils.web.TestWebServer;

/**
 * DiagStatusCommandTest for {@link DiagStatusCommand}.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
public class DiagStatusCommandTest {

    static TestWebServer webServer;
    static DiagStatusService diagStatusService;
    static SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);
    static DiagStatusCommand diagStatusCommand;
    static ClusterMemberInfo clusterMemberInfo = mock(ClusterMemberInfo.class);
    static HttpClient httpClient =  new HttpClient(ImmutableMap.of("org.osgi.service.http.port", "8181"));

    static String serviceStatusSummary = "Node IP Address: {node-ip}\n"
            + "System is operational: true\n"
            + "System ready state: ACTIVE\n"
            + "  testService         : OPERATIONAL   (operational)\n";

    static String servletContext = DiagStatusCommand.DIAGSTATUS_URL_SEPARATOR + DiagStatusCommand.DIAGSTATUS_URL_SUFFIX;

    @BeforeClass
    public static void start() throws Exception {
        diagStatusService = new DiagStatusServiceImpl(Collections.emptyList(), systemReadyMonitor);
        String testService1 = "testService";
        diagStatusService.register(testService1);
        diagStatusService.report(new ServiceDescriptor("testService", OPERATIONAL,
                "operational"));
        diagStatusCommand = new DiagStatusCommand(new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor),
                clusterMemberInfo, httpClient);
    }

    @AfterClass
    public static void afterTest() throws ServletException {
        webServer.close();
    }

    @Test
    // INFRAUTILS-56 mentions this test was previously ignored as the shutdown had issues due to the RMI connector
    // Hence trying to re-enable this as part of INFRAUTILS-45
    public void testGetRemoteStatusSummary_IPv4() throws Exception {
        webServer = new TestWebServer("127.0.0.1", httpClient.getHttpPort(), servletContext);
        webServer.registerServlet(new DiagStatusServlet(diagStatusService), "/*");
        checkGetRemoteStatusSummary(InetAddresses.forString("127.0.0.1"));
    }

    @Test
    public void testGetRemoteStatusSummary_IPv6() throws Exception {
        webServer = new TestWebServer("::1", httpClient.getHttpPort(), servletContext);
        webServer.registerServlet(new DiagStatusServlet(diagStatusService), "/*");
        checkGetRemoteStatusSummary(InetAddresses.forString("::1"));
    }

    private static void checkGetRemoteStatusSummary(InetAddress inetAddress) throws Exception {
        String actualServiceStatusSummary = diagStatusCommand.getRemoteStatusSummary(inetAddress);
        assertThat(serviceStatusSummary.replaceAll(
                ".*Node IP Address.*\\n", "Node IP Address: "
                        + inetAddress.toString() + "\n")).isEqualTo(actualServiceStatusSummary);
    }
}
