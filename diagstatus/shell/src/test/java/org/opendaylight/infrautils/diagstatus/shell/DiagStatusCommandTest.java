/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.infrautils.diagstatus.ServiceState.OPERATIONAL;

import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
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
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DiagStatusCommandTest {

    TestWebServer webServer;
    DiagStatusService diagStatusService;
    SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);
    DiagStatusCommand diagStatusCommand;
    @Mock
    ClusterMemberInfo clusterMemberInfo;
    DiagStatusServiceMBeanImpl diagStatusServiceMBeanImpl;

    DefaultHttpClientService httpClient;

    static String serviceStatusSummary = """
        Node IP Address: {node-ip}
        System is operational: true
        System ready state: ACTIVE
          testService         : OPERATIONAL   (operational)
        """;

    static String servletContext = DiagStatusCommand.DIAGSTATUS_URL_SEPARATOR + DiagStatusCommand.DIAGSTATUS_URL_SUFFIX;

    @Before
    public void start() throws Exception {
        diagStatusService = new DiagStatusServiceImpl(List.of(), systemReadyMonitor);
        String testService1 = "testService";
        ServiceRegistration reg = diagStatusService.register(testService1);
        reg.report(new ServiceDescriptor("testService", OPERATIONAL, "operational"));
        httpClient = new DefaultHttpClientService();
        httpClient.activate(Map.of("org.osgi.service.http.port", "8181"));
        diagStatusServiceMBeanImpl = new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor);
        diagStatusCommand = new DiagStatusCommand();
        diagStatusCommand.clusterMemberInfoProvider = clusterMemberInfo;
        diagStatusCommand.diagStatusServiceMBean = diagStatusServiceMBeanImpl;
        diagStatusCommand.httpClient = httpClient;
    }

    @After
    public void afterTest() throws ServletException, MBeanRegistrationException,
            MalformedObjectNameException, InstanceNotFoundException, IOException {
        webServer.close();
        diagStatusServiceMBeanImpl.close();
    }

    @Test
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

    private void checkGetRemoteStatusSummary(InetAddress inetAddress) throws Exception {
        assertEquals(serviceStatusSummary.replaceAll(
            ".*Node IP Address.*\\n", "Node IP Address: " + inetAddress.getHostAddress() + "\n"),
            diagStatusCommand.getRemoteStatusSummary(inetAddress));
    }
}
