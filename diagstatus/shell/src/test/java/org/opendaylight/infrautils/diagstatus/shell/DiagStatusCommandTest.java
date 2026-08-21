/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
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
class DiagStatusCommandTest {
    private final SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);

    private DiagStatusService diagStatusService;
    private DiagStatusCommand diagStatusCommand;
    private DiagStatusServiceMBeanImpl diagStatusServiceMBeanImpl;

    @BeforeEach
    void beforeEach() {
        diagStatusService = new DiagStatusServiceImpl(systemReadyMonitor, List.of());
        var reg = diagStatusService.register("testService");
        reg.report(new ServiceDescriptor("testService", ServiceState.OPERATIONAL, "operational"));
        diagStatusServiceMBeanImpl = assertDoesNotThrow(
            () -> new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor));
        diagStatusCommand = new DiagStatusCommand();
        diagStatusCommand.diagStatusServiceMBean = diagStatusServiceMBeanImpl;
    }

    @AfterEach
    void afterEach() throws Exception {
        assertDoesNotThrow(diagStatusServiceMBeanImpl::close);
    }

    @Test
    void testGetRemoteStatusSummary_IPv4() {
        checkGetRemoteStatusSummary(InetAddresses.forString("127.0.0.1"));
    }

    @Test
    void testGetRemoteStatusSummary_IPv6() {
        checkGetRemoteStatusSummary(InetAddresses.forString("::1"));
    }

    private void checkGetRemoteStatusSummary(InetAddress inetAddress) {
        assertEquals("""
            Node IP Address: {node-ip}
            System is operational: true
            System ready state: ACTIVE
              testService         : OPERATIONAL   (operational)
            """.replaceAll(".*Node IP Address.*\\n", "Node IP Address: " + inetAddress.getHostAddress() + "\n"),
            assertDoesNotThrow(() -> diagStatusCommand.getLocalStatusSummary(inetAddress)));
    }
}
