/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.sample;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.opendaylight.infrautils.metrics.prometheus.impl.CollectorRegistrySingleton;
import org.opendaylight.infrautils.metrics.prometheus.impl.PrometheusMetricProviderImpl;

/**
 * Launcher for and demo of simple standalone metrics example reporting to Prometheus.
 *
 * @author Michael Vorburger.ch
 */
public final class MetricsPrometheusExampleMain {
    private MetricsPrometheusExampleMain() {

    }

    public static void main(String[] args) throws IOException {
        // see also OsgiWebInitializer
        CollectorRegistry collectorRegistry = new CollectorRegistrySingleton();
        PrometheusMetricProviderImpl metricProvider = new PrometheusMetricProviderImpl(collectorRegistry);
        try (MetricsExample metricsExample = new MetricsExample(metricProvider)) {
            HTTPServer server = new HTTPServer(new InetSocketAddress("localhost", 1234), collectorRegistry);
            System.in.read();
            server.stop();
        }
        metricProvider.close();
    }
}
