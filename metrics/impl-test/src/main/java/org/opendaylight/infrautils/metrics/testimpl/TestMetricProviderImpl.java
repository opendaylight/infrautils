/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.testimpl;

import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.Timer;
import org.opendaylight.infrautils.utils.function.CheckedCallable;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;

/**
 * Implementation of {@link MetricProvider} useful for unit and component tests.
 * This is a simplistic yet fully working basic implementation.
 *
 * @author Michael Vorburger.ch
 */
public class TestMetricProviderImpl implements MetricProvider {

    @Override
    public Meter newMeter(Object anchor, String id) {
        return new Meter() {

            private final AtomicLong meter = new AtomicLong(0);

            @Override
            public void mark(long howMany) {
                meter.addAndGet(howMany);
            }

            @Override
            public long get() {
                return meter.get();
            }

            @Override
            public void close() {
                // ignore
            }
        };
    }

    @Override
    public Counter newCounter(Object anchor, String id) {
        return new Counter() {

            private final AtomicLong meter = new AtomicLong(0);

            @Override
            public void increment(long howMany) {
                meter.addAndGet(howMany);
            }

            @Override
            public void decrement(long howMany) {
                meter.addAndGet(-howMany);
            }

            @Override
            public long get() {
                return meter.get();
            }

            @Override
            public void close() {
                // ignore
            }
        };
    }

    @Override
    public Timer newTimer(Object anchor, String id) {
        return new Timer() {

            @Override
            public void close() {
            }

            @Override
            public <E extends Exception> void time(CheckedRunnable<E> event) throws E {
                event.run();
            }

            @Override
            public <T, E extends Exception> T time(CheckedCallable<T, E> event) throws E {
                return event.call();
            }
        };
    }

}
