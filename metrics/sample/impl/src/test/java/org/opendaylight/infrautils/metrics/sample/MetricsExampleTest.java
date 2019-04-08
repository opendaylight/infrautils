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

    @Rule public LogCaptureRule logCaptureRule = new LogCaptureRule();

    @Test
    public void testMetricsExample() throws InterruptedException {
        MetricProviderImpl metricsProvider = new MetricProviderImpl();
        MetricsExample metricsExample = new MetricsExample(metricsProvider);
        // do NOT metricsExample.init(); instead just make it use the Metrics API once:
        metricsExample.run();
        metricsExample.close();
        metricsProvider.close();
    }

}
