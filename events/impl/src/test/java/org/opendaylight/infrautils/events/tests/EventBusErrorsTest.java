/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import static org.junit.Assert.fail;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Test;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.events.guavafork.internal.EventBusImpl;

/**
 * Tests the error handling incl. lifecycle of the {@link EventBus}.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
public class EventBusErrorsTest {

    @Test(expected = IllegalStateException.class)
    public void testClosedSubscribeShouldThrowException() throws Exception {
        EventBus bus = new EventBusImpl();
        bus.close();
        bus.subscribe(new Object() {
            @Listener void onAnyEvent(Object event) {
            }
        });
    }

    @Test
    public void testClosedShouldNotThrowExceptionButReturnFailedFutureOnPublish() throws Exception {
        EventBusImpl bus = new EventBusImpl();
        bus.init();
        bus.close();
        assertFutureCompletedExceptionally(bus.publish(new Object()));
    }

    @Test
    public void testExceptionInListener() throws Exception {
        try (EventBusImpl bus = new EventBusImpl()) {
            bus.init();
            bus.subscribe(new Object() {
                @Listener void onNewFile(File file) throws IOException {
                    throw new IOException("boum");
                }
            });
            assertFutureCompletedExceptionally(bus.publish(new File(".")));
        }
    }

    private void assertFutureCompletedExceptionally(CompletableFuture<Void> future) {
        try {
            future.join();
            fail("CompletionException expected");
        } catch (CompletionException e) {
            // as expected
        }
    }

}
