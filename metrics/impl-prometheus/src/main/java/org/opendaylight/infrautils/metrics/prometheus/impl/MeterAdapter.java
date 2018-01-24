/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter.Child;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;

/**
 * Package private {@link Meter} adapter.
 *
 * @author Michael Vorburger.ch
 */
class MeterAdapter implements Meter {
    // TODO re-use metrics.impl CounterImpl extends CloseableMetricImpl by intro. metrics.spi

    // private final CollectorRegistry prometheusRegistry;
    private final io.prometheus.client.Counter prometheusCounter;
    private final @Nullable Child prometheusChild;

    MeterAdapter(CollectorRegistry prometheusRegistry, MetricDescriptor descriptor, List<String> labelNames,
            List<String> labelValues) {
        // this.prometheusRegistry = prometheusRegistry;
        this.prometheusCounter = io.prometheus.client.Counter.build()
                // https://prometheus.io/docs/practices/naming/#metric-names: "application prefix relevant to
                // the domain the metric belongs to. The prefix is sometimes referred to as namespace by client
                // libraries. For metrics specific to an application, the prefix is usually the application name."
                .namespace("opendaylight")
                .subsystem(descriptor.project())
                .name(descriptor.module() + "_" + descriptor.id())
                .help(descriptor.description())
                .labelNames(labelNames.toArray(new String[labelNames.size()]))
                .register(prometheusRegistry);
        if (!labelValues.isEmpty()) {
            this.prometheusChild = prometheusCounter.labels(labelValues.toArray(new String[labelValues.size()]));
        } else {
            this.prometheusChild = null;
        }
    }

    @Override
    public void close() {
        // TODO This... is a PITA - we should only unregister after the last of many label'd metric is unregistered...
        // prometheusRegistry.unregister(prometheusCounter);
    }

    @Override
    public void mark(long howMany) {
        if (prometheusChild != null) {
            prometheusChild.inc(howMany);
        } else {
            prometheusCounter.inc(howMany);
        }
    }

    @Override
    public long get() {
        // TODO see PrometheusMetricProviderImplTest.testGetOverflownMeter
        // TODO Is this cast from double to long safe?? We only ever increment by long howMany, but..
        // it could overflow!  So use Double.doubleToRawLongBits / doubleToLongBits?
        if (prometheusChild != null) {
            return (long) prometheusChild.get();
        } else {
            return (long) prometheusCounter.get();
        }
    }
}
