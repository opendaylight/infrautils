/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.MetricProvider;

/**
 * Implementation of {@link MetricProvider} based on <a href="https://prometheus.io">Prometheus.IO</a>.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class PrometheusMetricProviderImpl extends AbstractPrometheusMetricProvider {
    /**
     * Constructor. We force passing an existing CollectorRegistry instead of
     * CollectorRegistry.defaultRegistry because defaultRegistry is static, which is
     * a problem for use e.g. in tests.
     */
    @Inject
    public PrometheusMetricProviderImpl(CollectorRegistry prometheusRegistry) {
        super(prometheusRegistry);
    }

    @PreDestroy
    public void close() {
        prometheusRegistry.clear();
    }
}
