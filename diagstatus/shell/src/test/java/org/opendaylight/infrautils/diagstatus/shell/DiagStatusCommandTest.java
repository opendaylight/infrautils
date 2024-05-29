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
import java.net.InetAddress;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceRegistration;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceImpl;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor.Behaviour;

/**
 * DiagStatusCommandTest for {@link DiagStatusCommand}.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
public class DiagStatusCommandTest {
    private final SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);

    private DiagStatusService diagStatusService;
    private DiagStatusCommand diagStatusCommand;
    private DiagStatusServiceMBeanImpl diagStatusServiceMBeanImpl;

    @Before
    public void start() throws Exception {
        diagStatusService = new DiagStatusServiceImpl(systemReadyMonitor, List.of());
        String testService1 = "testService";
        ServiceRegistration reg = diagStatusService.register(testService1);
        reg.report(new ServiceDescriptor("testService", OPERATIONAL, "operational"));
        diagStatusServiceMBeanImpl = new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor);
        diagStatusCommand = new DiagStatusCommand();
        diagStatusCommand.diagStatusServiceMBean = diagStatusServiceMBeanImpl;
    }

    @After
    public void afterTest() throws Exception {
        diagStatusServiceMBeanImpl.close();
    }

    @Test
    public void testGetRemoteStatusSummary_IPv4() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("127.0.0.1"));
    }

    @Test
    public void testGetRemoteStatusSummary_IPv6() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("::1"));
    }

    private void checkGetRemoteStatusSummary(InetAddress inetAddress) throws Exception {
        assertEquals("""
            Node IP Address: {node-ip}
            System is operational: true
            System ready state: ACTIVE
              testService         : OPERATIONAL   (operational)
            """.replaceAll(".*Node IP Address.*\\n", "Node IP Address: " + inetAddress.getHostAddress() + "\n"),
            diagStatusCommand.getLocalStatusSummary(inetAddress));
    }
}
