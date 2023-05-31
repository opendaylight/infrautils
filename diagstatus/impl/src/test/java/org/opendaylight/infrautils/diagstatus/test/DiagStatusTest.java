/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import javax.inject.Inject;
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

    @Rule public LogRule logRule = new LogRule();
    @Rule public LogCaptureRule logCaptureRule = new LogCaptureRule();
    @Rule public MethodRule guice = new GuiceRule(new DiagStatusTestModule());

    @Inject DiagStatusService diagStatusService;
    @Inject DiagStatusServiceMBean diagStatusServiceMBean;

    private static final String SERVICE_STATUS_SUMMARY = """
        {
          "timeStamp": "{DO-NOT-BOTHER}",
          "isOperational": false,
          "systemReadyState": "ACTIVE",
          "systemReadyStateErrorCause": "",
          "statusSummary": [
            {
              "serviceName": "testService",
              "effectiveStatus": "UNREGISTERED",
              "reportedStatusDescription": "service is Unregistered",
              "statusTimestamp": "{DO-NOT-BOTHER}",
              "errorCause": null
            }
          ]
        }""";

    @Test
    public void testDiagStatus() {
        String testService1 = "testService";
        diagStatusService.register(testService1);
        // Verify if "testService" got registered with STARTING state.
        ServiceDescriptor serviceDescriptor1 = diagStatusService.getServiceDescriptor(testService1);
        assertEquals(ServiceState.STARTING, serviceDescriptor1.getServiceState());
        assertFalse(diagStatusService.getServiceStatusSummary().isOperational());

        // Verify that we get _something_ from getErrorCause()
        assertEquals(Optional.empty(), serviceDescriptor1.getErrorCause());

        // Verify if "testService" status is updated as OPERATIONAL.
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1, ServiceState.OPERATIONAL,
                "service is UP");
        diagStatusService.report(reportStatus);
        ServiceDescriptor serviceDescriptor2 = diagStatusService.getServiceDescriptor(testService1);
        assertEquals(ServiceState.OPERATIONAL, serviceDescriptor2.getServiceState());
        assertTrue(diagStatusService.getServiceStatusSummary().isOperational());

        // Verify if "testService" status is updated as UNREGISTERED.
        diagStatusService.report(new ServiceDescriptor(testService1, ServiceState.UNREGISTERED,
                "service is Unregistered"));
        assertFalse(diagStatusService.getServiceStatusSummary().isOperational());

        // JMX based Junits to see if the service state is getting retrieved properly.
        assertEquals(ServiceState.UNREGISTERED.name(),
               diagStatusServiceMBean.acquireServiceStatusMap().get(testService1));

        // Description must be shown
        assertThat(diagStatusServiceMBean.acquireServiceStatusDetailed(), containsString("service is Unregistered"));

        String actualServiceStatusSummary = diagStatusService.getServiceStatusSummary().toJSON();
        assertEquals(SERVICE_STATUS_SUMMARY, actualServiceStatusSummary.replaceAll(
                "\"timeStamp\":.*\\n", "\"timeStamp\": \"{DO-NOT-BOTHER}\",\n")
                .replaceAll("\"statusTimestamp\":.*\\n",
                        "\"statusTimestamp\": \"{DO-NOT-BOTHER}\",\n"));
    }

    @Test
    public void testErrorCause() {
        String testService1 = "testService";
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1,
                new NullPointerException("This is totally borked!"));

        assertEquals("This is totally borked!", reportStatus.getErrorCause().orElseThrow().getMessage());
    }
}
