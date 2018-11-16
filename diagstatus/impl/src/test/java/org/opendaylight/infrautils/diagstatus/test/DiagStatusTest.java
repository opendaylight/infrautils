/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.test;

import static com.google.common.truth.Truth.assertThat;

import java.util.Optional;
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

    @Inject DiagStatusService diagStatusService;
    @Inject DiagStatusServiceMBean diagStatusServiceMBean;

    private static final String SERVICE_STATUS_SUMMARY = "{\n"
            + "  \"timeStamp\": \"Fri Nov 16 17:34:26 IST 2018\",\n"
            + "  \"isOperational\": false,\n"
            + "  \"systemReadyState\": \"ACTIVE\",\n"
            + "  \"systemReadyStateErrorCause\": \"\",\n"
            + "  \"statusSummary\": [\n"
            + "    {\n"
            + "      \"serviceName\": \"testService\",\n"
            + "      \"effectiveStatus\": \"UNREGISTERED\",\n"
            + "      \"reportedStatusDescription\": \"service is Unregistered\",\n"
            + "      \"statusTimestamp\": \"2018-11-16T12:04:26.763Z\",\n"
            + "      \"errorCause\": \"\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testDiagStatus() {
        String testService1 = "testService";
        diagStatusService.register(testService1);
        // Verify if "testService" got registered with STARTING state.
        ServiceDescriptor serviceDescriptor1 = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(ServiceState.STARTING, serviceDescriptor1.getServiceState());
        assertThat(diagStatusService.isOperational()).isFalse();

        // JSON should be formatted
        assertThat(diagStatusService.getAllServiceDescriptorsAsJSON()).contains("\n");

        // Verify that we get _something_ from getErrorCause()
        assertThat(serviceDescriptor1.getErrorCause()).isEqualTo(Optional.empty());

        // Verify if "testService" status is updated as OPERATIONAL.
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1, ServiceState.OPERATIONAL,
                "service is UP");
        diagStatusService.report(reportStatus);
        ServiceDescriptor serviceDescriptor2 = diagStatusService.getServiceDescriptor(testService1);
        Assert.assertEquals(ServiceState.OPERATIONAL, serviceDescriptor2.getServiceState());
        assertThat(diagStatusService.isOperational()).isTrue();

        // Verify if "testService" status is updated as UNREGISTERED.
        diagStatusService.report(new ServiceDescriptor(testService1, ServiceState.UNREGISTERED,
                "service is Unregistered"));
        assertThat(diagStatusService.isOperational()).isFalse();

        // JMX based Junits to see if the service state is getting retrieved properly.
        Assert.assertEquals(ServiceState.UNREGISTERED.name(),
               diagStatusServiceMBean.acquireServiceStatusMap().get(testService1));

        // Description must be shown
        assertThat(diagStatusServiceMBean.acquireServiceStatusDetailed()).contains("service is Unregistered");

        assertThat(SERVICE_STATUS_SUMMARY.equals(diagStatusService.getAllServiceDescriptorsAsJSON()));
    }

    @Test
    public void testErrorCause() {
        String testService1 = "testService";
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1,
                new NullPointerException("This is totally borked!"));

        assertThat(reportStatus.getErrorCause().get().getMessage()).isEqualTo("This is totally borked!");
    }
}
