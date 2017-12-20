/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.opendaylight.infrautils.testutils.Asserts.assertThrows;

import com.codahale.metrics.Meter;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.internal.MetricProviderImpl;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Unit Test for MetricProviderImpl.
 *
 * @author Michael Vorburger.ch
 */
public class MetricProviderTest {

    public @Rule LogRule logRule = new LogRule();

    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();

    private final MetricProvider metrics = new MetricProviderImpl();

    @After
    public void afterEachTest() {
        ((MetricProviderImpl) metrics).close();
    }

    @Test
    public void testMetricProviderImpl() {
        Meter meter1 = metrics.newMeter(this, "test.meter1");
        meter1.mark();
        meter1.mark();
        meter1.mark();
        assertThat(meter1.getCount()).isEqualTo(3);
    }

    @Test
    public void testDupeMeterID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            metrics.newMeter(this, "test.meter1");
        });
    }

    @Test
    public void testDupeAnyID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            // NB: We cannot register a Counter (not a Meter) with the same ID, either
            metrics.newCounter(this, "test.meter1");
        });
    }

    // TODO testCloseMeter() - newMeter, close it, same ID should work again

    // TODO testBadID .. startsWith("odl") no spaces only dots
    // TODO             also enforce all lower case except String after last dot (before is package)
    // TODO             also enforce only 4 parts? instead of String id have String project, String "bundle" (feature) ?

    // TODO testReadJMX() using org.opendaylight.infrautils.utils.management.MBeanUtil from https://git.opendaylight.org/gerrit/#/c/65153/


}
