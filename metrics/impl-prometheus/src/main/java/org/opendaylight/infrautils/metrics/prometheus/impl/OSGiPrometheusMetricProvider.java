/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import javax.servlet.ServletException;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = MetricProvider.class)
public final class OSGiPrometheusMetricProvider extends AbstractPrometheusMetricProvider {
    // FIXME: we really should be using HTTP whiteboard for this
    public static final String PROMETHEUS_METRICS_URL = "/metrics/prometheus";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiPrometheusMetricProvider.class);

    @Reference
    HttpService osgiHttpService = null;

    public OSGiPrometheusMetricProvider() {
        super(new CollectorRegistry(true));
    }

    @Activate
    void activate() throws ServletException, NamespaceException {
        MetricsServlet metricsServlet = new MetricsServlet(prometheusRegistry);
        osgiHttpService.registerServlet(PROMETHEUS_METRICS_URL, metricsServlet, null, null);
        LOG.info("Metrics for Prometheus scrape now exposed on: {}", PROMETHEUS_METRICS_URL);
    }

    @Deactivate
    void deactivate() {
        osgiHttpService.unregister(PROMETHEUS_METRICS_URL);
        prometheusRegistry.clear();
    }
}
