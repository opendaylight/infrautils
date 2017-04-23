/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import org.junit.After;
import org.junit.Before;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.internal.EventBusImpl;

/**
 * Some concepts shared among tests here.
 * @author Michael Vorburger.ch
 */
public abstract class EventsTestBase {

    protected abstract static class ParentEvent { }

    protected static class ChildEvent extends ParentEvent { }

    protected interface AnotherEvent { }

    protected EventBus bus;

    // EventBus is asynchronous, must use volatile:
    volatile boolean childEventReceived = false;
    volatile boolean parentEventReceived = false;
    volatile boolean anotherEventReceived = false;

    @Before
    public void setUp() {
        bus = new EventBusImpl();
    }

    @After
    public void tearDown() throws Exception {
        bus.close();
    }

}
