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
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.prometheus.impl.CollectorRegistrySingleton;
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
        new PrometheusMetricProviderImpl(new CollectorRegistrySingleton());
    }

    @Test
    public void testNewMeter() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl(new CollectorRegistrySingleton());
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build());
        meter.mark(123);
        assertThat(meter.get()).isEqualTo(123L);
        meter.close();
    }

    @Test
    public void testNewMeterWith1FixedLabel() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl(new CollectorRegistrySingleton());
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build(),
                "label1").label("value1");
        meter.mark(123);
        assertThat(meter.get()).isEqualTo(123L);
        meter.close();
    }

    @Test
    public void testNewMeterWith1DynamicLabel() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl(new CollectorRegistrySingleton());
        Labeled<Meter> meterWithLabel = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "jobKey");

        Meter meterA = meterWithLabel.label("ABC");
        meterA.mark(3);
        assertThat(meterA.get()).isEqualTo(3);

        Meter meterB = meterWithLabel.label("DEF");
        meterB.mark(1);
        assertThat(meterB.get()).isEqualTo(1);
        assertThat(meterA.get()).isEqualTo(3);

        Meter againMeterA = meterWithLabel.label("ABC");
        assertThat(againMeterA.get()).isEqualTo(3);
    }

    @Test
    public void testGetOverflownMeter() {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl(new CollectorRegistrySingleton());
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build());
        meter.mark(Double.doubleToRawLongBits(Double.MAX_VALUE));
        assertThat(meter.get()).isGreaterThan(1000000L);
    }

    // TODO more..

}
