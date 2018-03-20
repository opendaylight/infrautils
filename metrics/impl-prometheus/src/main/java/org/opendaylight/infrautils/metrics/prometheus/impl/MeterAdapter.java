/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.Counter.Child;
import java.util.List;
import org.opendaylight.infrautils.metrics.Meter;

/**
 * Package private {@link Meter} adapter.
 *
 * @author Michael Vorburger.ch
 */
class MeterAdapter implements Meter {
    // TODO re-use metrics.impl CounterImpl extends CloseableMetricImpl by intro. metrics.spi

    private final Child prometheusChild;

    MeterAdapter(io.prometheus.client.Counter prometheusCounter, List<String> labelValues) {
        if (labelValues.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.prometheusChild = prometheusCounter.labels(labelValues.toArray(new String[labelValues.size()]));
    }

    @Override
    public void close() {
        // TODO This... is a PITA - we should only unregister after the last of many label'd metric is unregistered...
        // prometheusRegistry.unregister(prometheusCounter);
    }

    @Override
    public void mark(long howMany) {
        prometheusChild.inc(howMany);
    }

    @Override
    public long get() {
        // TODO see PrometheusMetricProviderImplTest.testGetOverflownMeter
        // TODO Is this cast from double to long safe?? We only ever increment by long howMany, but..
        // it could overflow!  So use Double.doubleToRawLongBits / doubleToLongBits?
        // apply the same in MeterNoChildAdapter
        return (long) prometheusChild.get();
    }
}
