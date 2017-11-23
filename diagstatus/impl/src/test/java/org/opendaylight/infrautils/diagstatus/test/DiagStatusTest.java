/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.test;

import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Component tests for diagstatus.
 *
 * @author Faseela K
 */
public class DiagStatusTest {

    public @Rule LogRule logRule = new LogRule();
    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();
    public @Rule MethodRule guice = new GuiceRule(new DiagStatusTestModule());

    @Inject
    DiagStatusService diagStatusService;

    @Inject
    DiagStatusServiceMBean diagStatusServiceMBean;

    @Test
    public void testDiagStatus() {
        String testService1 = "testService";
        diagStatusService.register(testService1);
        // Verify if "testService" got registered with STARTING state.
        ServiceDescriptor serviceDescriptor = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(serviceDescriptor.getServiceState(), ServiceState.STARTING);

        // Verify if "testService" status is updated as OPERATIONAL.
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1, ServiceState.OPERATIONAL,
                "service is UP");
        diagStatusService.report(reportStatus);
        serviceDescriptor = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(serviceDescriptor.getServiceState(), ServiceState.OPERATIONAL);

        // What is the right way to do verification?
        diagStatusServiceMBean.acquireServiceStatusDetailed();

        // TODO add JXM based Junits to see if the service state is getting retrieved properly.
    }
}
