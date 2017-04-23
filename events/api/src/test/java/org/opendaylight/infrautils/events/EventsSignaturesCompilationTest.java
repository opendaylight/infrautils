/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.VerboseMockitoJUnitRunner;
import org.opendaylight.infrautils.events.karaf.KarafBootingEvent;
import org.opendaylight.infrautils.events.karaf.KarafBootingFailedEvent;
import org.opendaylight.infrautils.events.karaf.KarafBootingFailedEventBuilder;
import org.opendaylight.infrautils.events.karaf.KarafBootingSuccessfulEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test to verify events methods signatures re. generics usage.
 * This test contains no running {@literal @}Test methods; its point
 * is just to make sure this code compiles.
 *
 * @author Michael Vorburger.ch
 */
@RunWith(VerboseMockitoJUnitRunner.class)
public class EventsSignaturesCompilationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventsSignaturesCompilationTest.class);

    @Mock EventPublisher pub;
    @Mock EventSubscriber sub;

    @Test
    public void compile() {
        sub.subscribe(KarafBootingSuccessfulEvent.class, e -> {
        });

        // make sure we can subscribe a listener of a super type to a sub type event class
        sub.subscribe(KarafBootingFailedEvent.class, new EventListener<KarafBootingEvent>() {

            @Override
            public void on(KarafBootingEvent event) {
            }

            // dummy method just to make sure that this does not become a lambda
            private void foo() { }
        });

        sub.subscribe(Object.class, e -> {
            LOG.info("{}", e);
        });

        pub.publish(new KarafBootingSuccessfulEvent());

        pub.publish(new KarafBootingFailedEventBuilder().shortSummary("failed").longDetails("because..").build());

        // publish a (fictitious) "new Thread" event, just to make sure that
        // Events don't need to implement any marker interface
        pub.publish(new Thread("."));

    }
}
