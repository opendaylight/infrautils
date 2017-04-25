/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.events.guavafork.internal.EventBusImpl;

/**
 * Unit test async events chained via Futures (CompletionStage).
 *
 * @author Michael Vorburger.ch
 */
public class ChainedEventsTest {

    // public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule();
    // public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    EventBus bus = new EventBusImpl();

    AtomicInteger numberOfSomeEventsReceived = new AtomicInteger();

    static class SomeEvent {
    }

    @Listener
    CompletableFuture<Void> onSomeEvent(SomeEvent someEvent) throws InterruptedException {
        numberOfSomeEventsReceived.incrementAndGet();
        // Thread.sleep(100);
        if (numberOfSomeEventsReceived.get() < 2) {
            return bus.publish(new SomeEvent());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Test
    public void testChainedEvent() throws Exception {
        ((EventBusImpl)bus).init();
        bus.subscribe(this);

        bus.publish(new SomeEvent()).join();
        assertThat(numberOfSomeEventsReceived.get()).isEqualTo(2);

        bus.close();

    }

    static class FailingEvent {
    }

    @Listener
    CompletableFuture<Void> onFailingEvent(FailingEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("BOUM!"));
        return future;
    }

    @Test
    public void testFailedChainedEvent() throws Exception {
        ((EventBusImpl)bus).init();
        bus.subscribe(this);
        try {
            bus.publish(new FailingEvent()).join();
            fail("expected exception");
        } catch (CompletionException e) {
            // OK
        }
    }
}
