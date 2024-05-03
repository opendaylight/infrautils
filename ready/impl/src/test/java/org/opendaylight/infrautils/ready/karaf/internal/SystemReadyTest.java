/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import static org.junit.Assert.assertEquals;

import javax.management.JMException;
import org.apache.karaf.bundle.core.BundleService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.ready.karaf.internal.KarafSystemReady.Config;
import org.opendaylight.infrautils.testutils.LogRule;
import org.osgi.framework.BundleContext;

/**
 * Component tests for system ready.
 *
 * @author Faseela K
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SystemReadyTest {
    @Mock
    private BundleContext bundleContext;
    @Mock
    private BundleService bundleService;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Config config;

    @Rule public LogRule logRule = new LogRule();

    @Test
    public void testMbeanRegistration() throws JMException {
        // Register the SystemState MBean
        KarafSystemReady systemReady = new KarafSystemReady(bundleService, bundleContext, config);

        // Check via strong interface if initial value of BOOTING is assigned
        assertEquals(SystemState.BOOTING, systemReady.getSystemState());

        // Check via JMX if initial value of BOOTING is assigned after registration
        assertEquals(SystemState.BOOTING.name(), systemReady.getMbean().readMBeanAttribute("SystemState"));
    }
}
