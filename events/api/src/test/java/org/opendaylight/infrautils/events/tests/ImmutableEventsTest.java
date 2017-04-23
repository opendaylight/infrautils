/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import org.junit.Test;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.karaf.NodeStartFailedEventImpl;
import org.opendaylight.infrautils.events.services.ServiceRegisteredEvent;
import org.opendaylight.infrautils.events.services.ServiceRegisteredEventImpl;
import org.opendaylight.infrautils.events.system.NodeStartFailedEvent;

/**
 * Unit test to verify Immutables.org gen. events methods signatures.
 * The main point here is is just to make sure this code compiles.
 *
 * @author Michael Vorburger.ch
 */
public class ImmutableEventsTest {

    @Test
    @SuppressWarnings("unused")
    public void testImplsAndBuilders() {
        NodeStartFailedEvent eventA;
        eventA = NodeStartFailedEventImpl.builder()
                .shortSummary("failed")
                .longDetails("because..").build();
        eventA = NodeStartFailedEventImpl.of("failed", "because..");

        ServiceRegisteredEvent<EventBus> eventB;
        eventB = ServiceRegisteredEventImpl.of(EventBus.class);
        eventB = ServiceRegisteredEventImpl.of(EventBus.class).withFilter("*");
        eventB = ServiceRegisteredEventImpl.<EventBus>builder().serviceType(EventBus.class).filter("*").build();
    }

}
