/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import static com.google.common.truth.Truth.assertThat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.events.EventSubscriber.Subscription;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.testutils.LogRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for events with normal non-reified types.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
public class EventsTest extends EventsTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EventsTest.class);

    public @Rule LogRule logRule = new LogRule();

    // public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule();
    // public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    @Listener
    void onChildEvent(ChildEvent event) throws InterruptedException {
        LOG.info("onChildEvent()");
        childEventReceived = true;
        Thread.sleep(100);
    }

    @Listener
    void onParentEvent(ParentEvent event) throws InterruptedException {
        LOG.info("onParentEvent()");
        parentEventReceived = true;
        Thread.sleep(100);
    }

    @Listener
    void onAnotherEvent(AnotherEvent event) throws InterruptedException {
        LOG.info("onAnotherEvent()");
        // This test is just to make the test fail should AnotherEvent be processed before
        // ChildEvent which is published first
        if (childEventReceived) {
            anotherEventReceived = true;
        }
        Thread.sleep(100);
    }

    @Test
    public void testListenerAnnotation() throws Exception {
        final Subscription sub = bus.subscribe(this);
        publishChildEventAndAssert();
        bus.unsubscribe(sub);
    }

    private void publishChildEventAndAssert() throws Exception {
        bus.publish(new ChildEvent()).join();
        assertThat(childEventReceived).isTrue();
        assertThat(parentEventReceived).isTrue();
        assertThat(anotherEventReceived).isFalse();
    }

    @Test
    @Ignore // TODO FIXME, if possible - or can we really not guarantee order if we want parallelism > 1 ?
    public void testListenerInvokationOrdering() throws Exception {
        bus.subscribe(this);
        bus.publish(new ChildEvent());
        bus.publish(new AnotherEvent() {}).join();
        assertThat(childEventReceived).isTrue();
        assertThat(parentEventReceived).isTrue();
        assertThat(anotherEventReceived).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoListenerAnnotations() {
        bus.subscribe(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadNoArgumentAnnotatedListenerMethod() {
        bus.subscribe(new Object() {
            @Listener
            void onChildEvent(ChildEvent event) {}

            @Listener
            void onAnotherEvent() {}

        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadTwoArgumentsAnnotatedListenerMethod() {
        bus.subscribe(new Object() {
            @Listener
            void onChildEvent(ChildEvent event) {}

            @Listener
            void onAnotherEvent(ChildEvent event1, ChildEvent event2) {}

        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadReturnTypeAnnotatedListenerMethod() {
        bus.subscribe(new Object() {
            @Listener
            void onChildEvent(ChildEvent event) {}

            @Listener
            int onAnotherEvent(ChildEvent event) {
                return 0;
            }
        });
    }
/*
    @Test
    public void testSubscribeLambdaListener() throws Exception {
        bus.subscribe(ChildEvent.class, e -> {
            childEventReceived = true;
        });
        bus.subscribe(ParentEvent.class, e -> {
            parentEventReceived = true;
        });
        bus.subscribe(AnotherEvent.class, e -> {
            parentEventReceived = true;
        });
        publishChildEventAndAssert();
    }
*/
    /**
     * Make sure we can subscribe a listener of a super type to a sub type event class.
     */
/*
    @Test
    public void testSubscribeSuperTypeListener() throws Exception {
        bus.subscribe(ChildEvent.class, new EventListener<ParentEvent>() {

            @Override
            public void on(ParentEvent event) {
                parentEventReceived = true;
                childEventReceived = true;
            }

            // dummy method just to make sure that this does not become
            // a lambda e.g. by some IDE's auto format clean up action ;)
            @SuppressWarnings("unused")
            private void foo() { }
        });
        publishChildEventAndAssert();
    }

    @Test
    public void testSubscribeObject() throws Exception {
        bus.subscribe(Object.class, e -> {
            LOG.info("{}", e);
            parentEventReceived = true;
            childEventReceived = true;
            // technically also anotherEventReceived = true
            // but don't just to simplify test and re-use publishChildEventAndAssert()
        });
        publishChildEventAndAssert();
    }
*/
    /**
     * Publish a (fictitious) "new Thread" event by just announcing actual
     * thread objects, just to make sure that Events don't need to implement any
     * marker interface, nor even have to be {@link java.io.Serializable}.
     */
/*
    @Test
    public void testPublishAnyKindOfObject() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        bus.subscribe(Thread.class, e -> {
            eventReceived.set(true);
        });
        bus.publish(new Thread("."));
        assertThat(eventReceived.get()).isTrue();
    }
*/
}
