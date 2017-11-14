/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.tests;

import static com.google.common.truth.Truth.assertThat;

import com.codahale.metrics.Meter;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.metrics.internal.MetricProviderImpl;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Unit Test for MetricProviderImpl.
 *
 * @author Michael Vorburger.ch
 */
public class MetricProviderTest {

    public @Rule LogRule logRule = new LogRule();

    private final MetricProviderImpl metrics = new MetricProviderImpl();

    @After
    public void afterEachTest() {
        metrics.close();
    }

    @Test
    public void testMetricProviderImpl() {
        Meter meter1 = metrics.newMeter(this, "test.meter1");
        meter1.mark();
        meter1.mark();
        meter1.mark();
        assertThat(meter1.getCount()).isEqualTo(3);
    }

    // TODO testDupeID

    // TODO testBadID .. startsWith("odl") no spaces only dots
    // TODO             also enforce all lower case except String after last dot (before is package)
    // TODO             also enforce only 4 parts? instead of String id have String project, String "bundle" (feature) ?

    // TODO testReadJMX() using org.opendaylight.infrautils.utils.management.MBeanUtil from https://git.opendaylight.org/gerrit/#/c/65153/


}
