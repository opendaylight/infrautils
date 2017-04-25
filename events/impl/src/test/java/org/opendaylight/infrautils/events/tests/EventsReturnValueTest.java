/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.events.guavafork.internal.EventBusImpl;

/**
 * Unit test async events chained via Futures (CompletionStage).
 *
 * @author Michael Vorburger.ch
 */
public class EventsReturnValueTest {

    EventBus bus = new EventBusImpl();

    static class SomeEvent {
    }

    @Listener
    CompletableFuture<Integer> onSomeEvent(SomeEvent someEvent) throws InterruptedException {
        return CompletableFuture.completedFuture(123);
    }

    @Test
    public void testChainedEvent() throws Exception {
        ((EventBusImpl)bus).init();
        bus.subscribe(this);

        assertThat(bus.publish(new SomeEvent()).get()).isEqualTo(123);

        bus.close();
    }
}
