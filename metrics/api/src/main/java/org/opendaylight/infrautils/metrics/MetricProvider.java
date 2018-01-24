/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory to obtain a new metric for use by application code.
 *
 * <p>This API is a mix of (parts of) the Coda Hale's Dropwizard's MetricRegistry, and Prometheus' API.
 * ODL application wanting to expose metrics are strongly encouraged to obtain new metric
 * instances through this factory, instead of directly using Dropwizard new MetricRegistry themselves.
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
 * used in the future to un-register metrics on bundle reload automatically instead of explicitly.
 *
 * <p>The id String parameter in each method must be globally unique; an <tt>IllegalArgumentException</tt> is thrown
 * if it has previously already been used.  The convention is to use
 * <i>odl.&lt;projectName&gt;.&lt;moduleName&gt;.&lt;metricName&gt;</i>,
 * so e.g. <tt>odl.infrautils.jobcoordinator.jobsPending</tt>.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface MetricProvider {

    // TODO @Deprecated
    Meter newMeter(Object anchor, String id);

    Meter newMeter(MetricDescriptor descriptor);

    Labeled<Meter> newMeter(MetricDescriptor descriptor, String firstLabelName);

    Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName, String secondLabelName);

    Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName);

    Counter newCounter(Object anchor, String id);

    // Counter newCounter(MetricDescriptor descriptor);

    Timer newTimer(Object anchor, String id);

    // Timer newTimer(MetricDescriptor descriptor);

    // TODO Histogram newHistogram(Object anchor, String id);

    // @SuppressWarnings("rawtypes")
    // TODO Gauge newGauge(Object anchor, String id, MetricSupplier<Gauge> supplier);
    // TODO write a test to clarify how to use this with a MetricSupplier; what's that for?

}
