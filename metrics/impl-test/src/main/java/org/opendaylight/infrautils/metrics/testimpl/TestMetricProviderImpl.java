/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.testimpl;

import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
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
    public Meter newMeter(@Nullable Object anchor, @Nullable String id) {
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
    public Meter newMeter(MetricDescriptor descriptor) {
        return newMeter((Object) null, (String) null);
    }

    @Override
    public Labeled<Meter> newMeter(MetricDescriptor descriptor, String labelName) {
        return labelValue -> newMeter((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        return firstLabelValue -> secondLabelValue -> newMeter((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> newMeter((Object) null, (String) null);
    }

    @Override
    public Counter newCounter(@Nullable Object anchor, @Nullable String id) {
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
    public Counter newCounter(MetricDescriptor descriptor) {
        return newCounter((Object) null, (String) null);
    }

    @Override
    public Labeled<Counter> newCounter(MetricDescriptor descriptor, String labelName) {
        return labelValue -> newCounter((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Counter>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                            String secondLabelName) {
        return firstLabelValue -> secondLabelValue -> newCounter((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Labeled<Counter>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                                     String secondLabelName, String thirdLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> newCounter((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Counter>>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                                         String secondLabelName, String thirdLabelName,
                                                         String fourthLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> fourthLabelValue -> newCounter((Object) null,
            (String) null);
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> newCounter(MetricDescriptor descriptor,
                                                                  String firstLabelName, String secondLabelName,
                                                                  String thirdLabelName, String fourthLabelName,
                                                                  String fifthLabelName) {
        return firstLabelValue -> secondLabelValue -> thirdLabelValue -> fourthLabelValue -> fifthLabelValue ->
            newCounter((Object) null, (String) null);
    }

    @Override
    public Timer newTimer(@Nullable Object anchor, @Nullable String id) {
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

    @Override
    public Timer newTimer(MetricDescriptor descriptor) {
        return newTimer((Object) null, (String) null);
    }

    @Override
    public Labeled<Timer> newTimer(MetricDescriptor descriptor, String labelName) {
        return labelValue -> newTimer((Object) null, (String) null);
    }

    @Override
    public Labeled<Labeled<Timer>> newTimer(MetricDescriptor descriptor, String firstLabelName,
                                                String secondLabelName) {
        return firstLabelValue -> secondLabelValue -> newTimer((Object) null, (String) null);
    }

}
