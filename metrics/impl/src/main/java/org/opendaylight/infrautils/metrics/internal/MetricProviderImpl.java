/*
 * Copyright (c) 2017 - 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.MetricProvider;

/**
 * Implementation of {@link MetricProvider} based on <a href="http://metrics.dropwizard.io">Coda Hale's Dropwizard Metrics</a>.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class MetricProviderImpl extends AbstractMetricProvider {
    public MetricProviderImpl() {

    }

    public MetricProviderImpl(Configuration configuration) {
        updateConfiguration(configuration);
    }

    @PostConstruct
    public void open() {
        start();
    }

    @PreDestroy
    public void close() {
        stop();
    }
}
