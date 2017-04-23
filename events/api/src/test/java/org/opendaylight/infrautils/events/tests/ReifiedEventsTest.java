/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.tests;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.opendaylight.infrautils.events.Listener;

/**
 * Unit test for reified events.
 * See <a href="https://github.com/google/guava/wiki/ReflectionExplained">Guava Reflection explained</a>
 * and Neal Gafter's <a href="http://gafter.blogspot.ch/2006/12/super-type-tokens.html">Super Type Tokens</a> (and
 * <a href="http://gafter.blogspot.ch/2007/05/limitation-of-super-type-tokens.html">Limitation of Super Type Tokens</a>
 * and <a href="http://gafter.blogspot.ch/2006/11/reified-generics-for-java.html">Reified Generics for Java</a>).
 *
 * @author Michael Vorburger.ch
 */
public class ReifiedEventsTest extends EventsTestBase {

    private static class ReifiedEvent<T extends ParentEvent> {
    }

    @Listener
    void onChildEvent(ReifiedEvent<ChildEvent> event) {
        childEventReceived = true;
    }

    @Listener
    void onParentEvent(ReifiedEvent<ParentEvent> event) {
        parentEventReceived = true;
    }

    @Listener
    void onAnotherEvent(AnotherEvent event) {
        anotherEventReceived = true;
    }

    private void publishChildEventAndAssert() throws Exception {
        bus.publish(new ReifiedEvent<ChildEvent>()).get();
        assertThat(childEventReceived).isTrue();
        assertThat(parentEventReceived).isTrue();
        assertThat(anotherEventReceived).isFalse();
    }

    @Test
    public void testReifiedListenerAnnotation() throws Exception {
        bus.subscribe(this);
        publishChildEventAndAssert();
    }

    @Test
    @SuppressWarnings("serial")
    public void testSubscribeReifiedLambdaListener() throws Exception {
        bus.subscribe(new TypeToken<ReifiedEvent<ChildEvent>>() {}, e -> {
            childEventReceived = true;
        });
        bus.subscribe(new TypeToken<ReifiedEvent<ParentEvent>>() {}, e -> {
            parentEventReceived = true;
        });
        bus.subscribe(AnotherEvent.class, e -> {
            parentEventReceived = true;
        });
        publishChildEventAndAssert();
    }

/* TODO
    @Test
    @SuppressWarnings("serial")
    public void testSubscribeSuperTypeListener() throws Exception {
        bus.subscribe(new TypeToken<ReifiedEvent<ChildEvent>>() {}, new EventListener<ReifiedEvent<ParentEvent>>() {

            @Override
            public void on(ReifiedEvent<ParentEvent> event) {
                parentEventReceived = true;
                childEventReceived = true;
            }

            // dummy method just to make sure that this does not become
            // a lambda e.g. by some IDE's auto format clean up action ;)
            private void foo() { }
        });
        publishChildEventAndAssert();
    }
*/
}
