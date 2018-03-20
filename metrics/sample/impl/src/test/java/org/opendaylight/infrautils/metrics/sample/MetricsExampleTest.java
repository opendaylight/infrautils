/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.sample;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.metrics.internal.MetricProviderImpl;
import org.opendaylight.infrautils.testutils.LogCaptureRule;

/**
 * Test for MetricsExample.
 *
 * @author Michael Vorburger.ch
 */
public class MetricsExampleTest {

    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();

    @Test
    public void testMetricsExample() throws InterruptedException {
        MetricProviderImpl metricsProvider = new MetricProviderImpl();
        MetricsExample metricsExample = new MetricsExample(metricsProvider);
        metricsExample.init();
        Thread.sleep(1000);
        metricsExample.close();
        metricsProvider.close();
    }

}
