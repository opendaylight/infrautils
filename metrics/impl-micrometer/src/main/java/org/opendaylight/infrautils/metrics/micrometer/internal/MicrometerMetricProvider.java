/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.micrometer.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.Timer;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;

/**
 * Infrautils Metrics API implemented based on
 * <a href="http://Micrometer.io">Micrometer.io</a>.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = MetricProvider.class)
public class MicrometerMetricProvider implements MetricProvider {

    // TODO make MetricProviderTest share-able so this (and current Prometheus) impl can re-use it

    // TODO probably need the same Map that we have in MetricProviderImpl and PrometheusMetricProviderImpl

    private final MeterRegistry meterRegistry;

    @Inject
    public MicrometerMetricProvider(CompositeMeterRegistrySingleton compositeRegistry) {
        MeterRegistry simpleRegistry = new SimpleMeterRegistry();
        compositeRegistry.add(simpleRegistry);
        meterRegistry = compositeRegistry;
    }

    private static String makeMicrometerName(MetricDescriptor descriptor) {
        // TODO Contribute support for descriptor.description() to Micrometer
        return "opendaylight." + descriptor.project() + "." + descriptor.module() + "."
                + descriptor.id().replace('_', '.');
    }

    @Override
    @Deprecated
    public Meter newMeter(Object anchor, String id) {
        throw new UnsupportedOperationException("TODO Remove this (use the non-@Deprecated alternative method)");
    }

    @Override
    public Meter newMeter(MetricDescriptor descriptor) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Meter> newMeter(MetricDescriptor descriptor, String labelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    @Deprecated
    public Counter newCounter(Object anchor, String id) {
        return null;
    }

    @Override
    public Counter newCounter(MetricDescriptor descriptor) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Counter> newCounter(MetricDescriptor descriptor, String labelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Counter>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Labeled<Counter>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Counter>>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName, String fourthLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> newCounter(MetricDescriptor descriptor,
            String firstLabelName, String secondLabelName, String thirdLabelName, String fourthLabelName,
            String fifthLabelName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    @Deprecated
    public Timer newTimer(Object anchor, String id) {
        throw new UnsupportedOperationException("TODO Remove this (use the non-@Deprecated alternative method)");
    }

    @Override
    public Timer newTimer(MetricDescriptor descriptor) {
        return new TimerAdapter(meterRegistry.timer(makeMicrometerName(descriptor)));
    }

    @Override
    public Labeled<Timer> newTimer(MetricDescriptor descriptor, String labelName) {
        return labelValue -> new TimerAdapter(
                meterRegistry.timer(makeMicrometerName(descriptor), labelName, labelValue));
    }

    @Override
    public Labeled<Labeled<Timer>> newTimer(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        return firstLabelValue -> secondLabelValue -> new TimerAdapter(meterRegistry.timer(
                makeMicrometerName(descriptor), firstLabelName, firstLabelValue, secondLabelName, secondLabelValue));
    }

}
