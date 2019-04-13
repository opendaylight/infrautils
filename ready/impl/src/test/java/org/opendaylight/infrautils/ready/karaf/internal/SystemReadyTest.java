/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import javax.management.JMException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Component tests for system ready.
 *
 * @author Faseela K
 */
public class SystemReadyTest {

    @Rule public LogRule logRule = new LogRule();

    @Test
    public void testMbeanRegistration() throws JMException {
        // Register the SystemState MBean
        KarafSystemReady systemReady = new KarafSystemReady(null, null);

        // Check via strong interface if initial value of BOOTING is assigned
        Assert.assertEquals(SystemState.BOOTING, systemReady.getSystemState());

        // Check via JMX if initial value of BOOTING is assigned after registration
        Assert.assertEquals(SystemState.BOOTING.name(), systemReady.getMbean().readMBeanAttribute("SystemState"));
    }
}
