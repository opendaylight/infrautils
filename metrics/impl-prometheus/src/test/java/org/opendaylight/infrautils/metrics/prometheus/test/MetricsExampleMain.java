/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.test;

import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.prometheus.impl.PrometheusMetricProviderImpl;

/**
 * Prometheus demo.
 *
 * @author Michael Vorburger.ch
 */
public class MetricsExampleMain {

    public static void main(String[] args) throws IOException {
        new MetricsExampleMain().run();
    }

    public void run() throws IOException {
        MetricProvider metricProvider = new PrometheusMetricProviderImpl();
        Meter meter = metricProvider.newMeter(this, "demo");
        meter.mark(23);

        // TODO new HTTPServer(InetSocketAddress addr, CollectorRegistry registry)
        HTTPServer server = new HTTPServer(1234);
        System.in.read();

        meter.close();
        server.stop();
    }
}
