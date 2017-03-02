/*
 * Copyright (c) 2017 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.metrics.internal;

import javax.inject.Singleton;

import org.opendaylight.infrautils.metrics.api.ICounter;
import org.opendaylight.infrautils.metrics.api.Metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

@Singleton
public class MetricsImpl implements Metrics {
    MetricRegistry mr = new MetricRegistry();

    @Override
    public ICounter getCounter(String name) {
        Counter c = mr.counter(name);
        CounterImpl counter = new CounterImpl(c);
        return counter;
    }
}
