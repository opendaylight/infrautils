/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.ClusterMemberInfo;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor;

/**
 * Test for {@link DiagStatusCommand}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusCommandTest {

    @Test
    public void testGetRemoteStatusSummary_IPv4() throws Exception {
        checkGetRemoteStatusSummary("127.0.0.1");
    }

    @Test
    public void testGetRemoteStatusSummary_IPv6() throws Exception {
        checkGetRemoteStatusSummary("::1");
    }

    private void checkGetRemoteStatusSummary(String ipAddress) throws Exception {
        DiagStatusService diagStatusService = mock(DiagStatusService.class);
        SystemReadyMonitor systemReadyMonitor = new TestSystemReadyMonitor();
        ClusterMemberInfo clusterMemberInfo = new ClusterMemberInfo() {
            @Override
            public String getSelfAddress() {
                return ipAddress;
            }

            @Override
            public List<String> getClusterMembers() {
                return emptyList();
            }
        };
        try (DiagStatusServiceMBeanImpl diagStatusServiceMBeanImpl =
                new DiagStatusServiceMBeanImpl(diagStatusService, systemReadyMonitor, clusterMemberInfo)) {
            DiagStatusCommand.getRemoteStatusSummary(ipAddress);
        }
    }
}
