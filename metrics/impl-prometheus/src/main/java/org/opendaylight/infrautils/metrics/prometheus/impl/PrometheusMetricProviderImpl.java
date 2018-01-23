/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.Timer;

/**
 * Implementation of {@link MetricProvider} based on <a href="https://prometheus.io">Prometheus.IO</a>.
 *
 * @author Michael Vorburger.ch
 */
public class PrometheusMetricProviderImpl implements MetricProvider {

    private final CollectorRegistry prometheusRegistry;

    public PrometheusMetricProviderImpl() {
        this(CollectorRegistry.defaultRegistry); // new CollectorRegistry(true); ?
    }

    public PrometheusMetricProviderImpl(CollectorRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
    }

    private io.prometheus.client.Counter newPrometheusCounter(Object anchor, String project, String module, String id,
            String description, String... labelNames) {
        // io.prometheus.client.Counter prometheusCounter =
        return io.prometheus.client.Counter.build()
                // https://prometheus.io/docs/practices/naming/#metric-names: "application prefix relevant to
                // the domain the metric belongs to. The prefix is sometimes referred to as namespace by client
                // libraries. For metrics specific to an application, the prefix is usually the application name."
                .namespace("opendaylight")
                .subsystem(project)
                .name(module + "_" + id)
                .help(description)
                .labelNames(labelNames)
                .register(prometheusRegistry);
    }

    @Override
    public Meter newMeter(Object anchor, String id) {
        // re-use metrics.impl CounterImpl extends CloseableMetricImpl by intro. metrics.spi
        return new Meter() {

            io.prometheus.client.Counter prometheusCounter = io.prometheus.client.Counter.build()
                    // https://prometheus.io/docs/practices/naming/#metric-names: "application prefix relevant to
                    // the domain the metric belongs to. The prefix is sometimes referred to as namespace by client
                    // libraries. For metrics specific to an application, the prefix is usually the application name."
                    .namespace("opendaylight").name(id).help(id)
                    .subsystem("theSubsystem")
                    // TODO .subsystem(subsystem) from MetricDescriptor.project()
                    .labelNames("interface")
                    .register(prometheusRegistry);

            @Override
            public void close() {
                prometheusRegistry.unregister(prometheusCounter);
            }

            @Override
            public void mark(long howMany) {
                prometheusCounter.labels("eth0").inc(howMany);
            }

            @Override
            public long get() {
                return (long) prometheusCounter.get();
            }
        };
    }

    @Override
    public Meter newMeter(MetricDescriptor descriptor) {
        // re-use metrics.impl CounterImpl extends CloseableMetricImpl by intro. metrics.spi
        return new Meter() {

            io.prometheus.client.Counter prometheusCounter = io.prometheus.client.Counter.build()
                    // https://prometheus.io/docs/practices/naming/#metric-names: "application prefix relevant to
                    // the domain the metric belongs to. The prefix is sometimes referred to as namespace by client
                    // libraries. For metrics specific to an application, the prefix is usually the application name."
                    .namespace("opendaylight")
                    .subsystem(descriptor.project())
                    .name(descriptor.module() + "_" + descriptor.id())
                    .help(descriptor.description())
                    .labelNames("interface")
                    .register(prometheusRegistry);

            @Override
            public void close() {
                prometheusRegistry.unregister(prometheusCounter);
            }

            @Override
            public void mark(long howMany) {
                prometheusCounter.labels("eth0").inc(howMany);
            }

            @Override
            public long get() {
                return (long) prometheusCounter.get();
            }
        };
    }

    @Override
    public Labeled<Meter> newMeter(MetricDescriptor descriptor, String firstLabelName) {
        return null;
    }

    @Override
    public Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName) {
        return null;
    }

    @Override
    public Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName) {
        return null;
    }

    @Override
    public Counter newCounter(Object anchor, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer newTimer(Object anchor, String id) {
        throw new UnsupportedOperationException();
    }

}
