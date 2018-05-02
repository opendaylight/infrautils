/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.test;

import static com.google.common.truth.Truth.assertThat;
import static org.opendaylight.infrautils.diagstatus.MBeanUtils.getJMXUrl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.opendaylight.infrautils.diagstatus.*;
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

    @Test
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

    }

    @Test
    public void testErrorCause() throws IOException, MalformedObjectNameException {
        String testService1 = "testService";
        ServiceDescriptor reportStatus = new ServiceDescriptor(testService1,
                new NullPointerException("This is totally borked!"));

        assertThat(reportStatus.getErrorCause().get().getMessage()).isEqualTo("This is totally borked!");

        JMXServiceURL url = getJMXUrl("127.0.0.1");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName mbeanObj = new ObjectName(MBeanUtils.JMX_OBJECT_NAME);
        DiagStatusServiceMBean mbeanProxy =
            JMX.newMBeanProxy(mbsc, mbeanObj, DiagStatusServiceMBean.class, true);
        String serviceStatus = mbeanProxy.acquireServiceStatusDetailed();
    }
}
