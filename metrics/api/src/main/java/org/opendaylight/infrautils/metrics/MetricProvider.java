/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.Timer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory to obtain a new metric for use by application code.
 *
 * <p>This is basically a mirror of (parts of) the Coda Hale's Dropwizard's MetricRegistry.
 * ODL application wanting to expose metrics are strongly encouraged to obtain new {@link Metric}
 * instances through this factory, instead of directly using new MetricRegistry themselves.
 * This allows infrautils.metrics to expose all applications' metrics together through
 * current and future reporters.  This API also includes some convenience such as preventing
 * accidental re-use of Metric IDs by different ODL applications, as well as (perhaps more importantly)
 * isolating ODL applications from each other and preventing one from grabbing and reading or worse
 * modifying another application's metric.  Metrics should only be used to expose from an application
 * to reporters registered centrally by infrautils.metrics, and are never exposed between applications.
 *
 * <p>The anchor Object parameter in each method is used to record which bundle registered the meter,
 * and you almost always just use <code>this</code> for that argument.
 * This is used in error messages if another bundle tries to register a duplicate ID, and could be
 * used in the future to un-register metrics on bundle reload.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface MetricProvider {

    Counter newCounter(Object anchor, String id);

    Timer newTimer(Object anchor, String id);

    Meter newMeter(Object anchor, String id);

    Histogram newHistogram(Object anchor, String id);

    @SuppressWarnings("rawtypes")
    Gauge newGauge(Object anchor, String id, MetricSupplier<Gauge> supplier);

}
