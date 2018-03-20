/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import java.util.List;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;

/**
 * Package private {@link Meter} holder of Prometheus Counter (i.e. infrautils.Meter),
 * without any child label values; instances of this are shared among several {@link MeterAdapter}.
 *
 * @author Michael Vorburger.ch
 */
@SuppressWarnings("javadoc")
class MeterNoChildAdapter implements Meter {

    final io.prometheus.client.Counter prometheusCounter;

    MeterNoChildAdapter(CollectorRegistry prometheusRegistry, MetricDescriptor descriptor, List<String> labelNames) {
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
    }

    @Override
    public void mark(long howMany) {
        prometheusCounter.inc(howMany);
    }

    @Override
    public long get() {
        // TODO see MeterAdapter#get
        return (long) prometheusCounter.get();
    }

    @Override
    public void close() {
        // TODO implement this correctly... see MeterAdapter#close
    }
}
