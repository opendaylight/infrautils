/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import javax.management.JMException;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.ready.karaf.internal.KarafSystemReady.Config;
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
        KarafSystemReady systemReady = new KarafSystemReady();
        systemReady.activate(null, new Config() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Config.class;
            }

            @Override
            public int systemReadyTimeout() {
                return 300;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this;
            }
        });

        // Check via strong interface if initial value of BOOTING is assigned
        assertEquals(SystemState.BOOTING, systemReady.getSystemState());

        // Check via JMX if initial value of BOOTING is assigned after registration
        assertEquals(SystemState.BOOTING.name(), systemReady.getMbean().readMBeanAttribute("SystemState"));
    }
}
