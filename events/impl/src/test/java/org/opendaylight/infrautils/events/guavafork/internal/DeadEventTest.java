/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.guavafork.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Tests that dead events are logged.
 *
 * @author Michael Vorburger.ch
 */
public class DeadEventTest {

    @Test
    public void testDeadEvent() throws Exception {
        EventBusImpl bus = new EventBusImpl();
        bus.init();
        bus.publish(new DeadEventTest()).get();
        assertThat(bus.lastDeadEvent).isInstanceOf(DeadEventTest.class);
        bus.close();
    }

}
