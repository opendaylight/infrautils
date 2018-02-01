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

    /**
     * Create new Meter metric.
     * @deprecated use {@link #newMeter(MetricDescriptor)} instead.
     */
    @Deprecated
    Meter newMeter(Object anchor, String id);

    /**
     * Create new Meter metric without labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @return the Meter
     */
    Meter newMeter(MetricDescriptor descriptor);

    /**
     * Create new Meter metric with 1 label.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param labelName name of the (only) label of this metric
     * @return an object from which a Meter can be obtained, given 1 label value
     */
    Labeled<Meter> newMeter(MetricDescriptor descriptor, String labelName);

    /**
     * Create new Meter metric with 2 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @return an object from which a Meter can be obtained, given 2 label values
     */
    Labeled<Labeled<Meter>> newMeter(MetricDescriptor descriptor, String firstLabelName, String secondLabelName);

    /**
     * Create new Meter metric with 3 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @param thirdLabelName name of the 3rd label of this metric
     * @return an object from which a Meter can be obtained, given 3 label values
     */
    Labeled<Labeled<Labeled<Meter>>> newMeter(MetricDescriptor descriptor, String firstLabelName,
            String secondLabelName, String thirdLabelName);

    /**
     * Create new Counter metric.
     * @deprecated use {@link #newCounter(MetricDescriptor)} instead.
     */
    @Deprecated
    Counter newCounter(Object anchor, String id);

    /**
     * Create new Counter metric without labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @return the Counter
     */
    Counter newCounter(MetricDescriptor descriptor);

    /**
     * Create new Counter metric with 1 label.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param labelName name of the (only) label of this metric
     * @return an object from which a Counter can be obtained, given 1 label value
     */
    Labeled<Counter> newCounter(MetricDescriptor descriptor, String labelName);

    /**
     * Create new Counter metric with 2 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @return an object from which a Counter can be obtained, given 2 label values
     */
    Labeled<Labeled<Counter>> newCounter(MetricDescriptor descriptor, String firstLabelName, String secondLabelName);

    /**
     * Create new Counter metric with 3 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @param thirdLabelName name of the 3rd label of this metric
     * @return an object from which a Counter can be obtained, given 3 label values
     */
    Labeled<Labeled<Labeled<Counter>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                              String secondLabelName, String thirdLabelName);

    /**
     * Create new Counter metric with 4 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @param thirdLabelName name of the 3rd label of this metric
     * @param fourthLabelName name of the 4th label of this metric
     * @return an object from which a Counter can be obtained, given 4 label values
     */
    Labeled<Labeled<Labeled<Labeled<Counter>>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                                       String secondLabelName, String thirdLabelName,
                                                       String fourthLabelName);

    /**
     * Create new Counter metric with 5 labels.
     * @param descriptor a MetricDescriptor, typically created via <code>MetricDescriptor.builder().anchor(this)
     *           .project("&lt;projectName&gt;").module("&lt;moduleName&gt;").id("&lt;metricName&gt;").build()</code>
     * @param firstLabelName name of the 1st label of this metric
     * @param secondLabelName name of the 2nd label of this metric
     * @param thirdLabelName name of the 3rd label of this metric
     * @param fourthLabelName name of the 4th label of this metric
     * @param fifthLabelName name of the 5th label of this metric
     * @return an object from which a Counter can be obtained, given 5 label values
     */
    Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> newCounter(MetricDescriptor descriptor, String firstLabelName,
                                                                String secondLabelName, String thirdLabelName,
                                                                String fourthLabelName, String fifthLabelName);

    Timer newTimer(Object anchor, String id);

    // Timer newTimer(MetricDescriptor descriptor);

    // TODO Histogram newHistogram(Object anchor, String id);

    // @SuppressWarnings("rawtypes")
    // TODO Gauge newGauge(Object anchor, String id, MetricSupplier<Gauge> supplier);
    // TODO write a test to clarify how to use this with a MetricSupplier; what's that for?

}
