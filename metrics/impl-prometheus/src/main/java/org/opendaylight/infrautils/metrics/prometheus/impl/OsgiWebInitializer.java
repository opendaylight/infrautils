/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the MetricsServlet.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class OsgiWebInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiWebInitializer.class);

    public static final String PROMETHEUS_METRICS_URL = "/metrics/prometheus";

    // This class ideally really should be using the WebServer/WebContext API
    // instead of directly the raw OSGi HttpService, but unfortunately that
    // ended up in AAA instead of infrautils for political reasons, and so
    // we cannot use it here... :-(

    private final HttpService osgiHttpService;
    private final CollectorRegistry collectorRegistry;

    @Inject
    public OsgiWebInitializer(@Reference HttpService osgiHttpService, CollectorRegistry collectorRegistry) {
        this.osgiHttpService = osgiHttpService;
        this.collectorRegistry = collectorRegistry;
    }

    @PostConstruct
    public void init() throws ServletException, NamespaceException {
        MetricsServlet metricsServlet = new MetricsServlet(collectorRegistry);
        osgiHttpService.registerServlet(PROMETHEUS_METRICS_URL, metricsServlet, null, null);
        LOG.info("Metrics for Prometheus scrape now exposed on: {}", PROMETHEUS_METRICS_URL);
    }

    @PreDestroy
    public void close() {
        osgiHttpService.unregister(PROMETHEUS_METRICS_URL);
    }

}
