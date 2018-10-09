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

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.diagstatus.spi.NoClusterMemberInfo;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor.Behaviour;

/**
 * Test for {@link DiagStatusCommand}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusCommandTest {

    @Test
    public void testGetRemoteStatusSummary_IPv4() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("127.0.0.1"));
    }

    @Test
    public void testGetRemoteStatusSummary_IPv6() throws Exception {
        checkGetRemoteStatusSummary(InetAddresses.forString("::1"));
    }

    private static void checkGetRemoteStatusSummary(InetAddress inetAddress) throws Exception {
        DiagStatusService diagStatusService = mock(DiagStatusService.class);
        SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor(Behaviour.IMMEDIATE);
        ClusterMemberInfo clusterMemberInfo = new NoClusterMemberInfo(inetAddress);
        try (DiagStatusServiceMBeanImpl diagStatusServiceMBeanImpl =
                new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor, clusterMemberInfo)) {
            assertThat(DiagStatusCommand.getRemoteStatusSummary(inetAddress)).contains(inetAddress.toString());
        }
    }
}
