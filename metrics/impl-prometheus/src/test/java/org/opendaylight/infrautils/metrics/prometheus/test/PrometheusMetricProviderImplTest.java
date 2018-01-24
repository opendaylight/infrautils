/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.test;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.prometheus.impl.PrometheusMetricProviderImpl;

/**
 * Unit test for {@link PrometheusMetricProviderImpl}.
 *
 * @author Michael Vorburger.ch
 */
public class PrometheusMetricProviderImplTest {

    // TODO share all @Test with existing MetricProviderTest through some refactoring

    @Test
    public void testNewMetricProvider() {
        new PrometheusMetricProviderImpl();
    }

    @Test
    public void testNewMeter() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl();
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build());
        meter.mark(123);
        assertThat(meter.get()).isEqualTo(123L);
        meter.close();
    }

    @Test
    public void testNewMeterWith1Label() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl();
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build(),
                "label1").label("value1");
        meter.mark(123);
        assertThat(meter.get()).isEqualTo(123L);
        meter.close();
    }

    @Test
    public void testGetOverflownMeter() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl();
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build());
        meter.mark(Double.doubleToRawLongBits(Double.MAX_VALUE));
        assertThat(meter.get()).isGreaterThan(1000000L);
    }

    // TODO more..

}
