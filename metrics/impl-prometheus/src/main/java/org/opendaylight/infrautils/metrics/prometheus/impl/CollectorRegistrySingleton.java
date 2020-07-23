/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.prometheus.impl;

import io.prometheus.client.CollectorRegistry;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides the Prometheus Client CollectorRegistry.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Named("collectorRegistrySingleton")
public class CollectorRegistrySingleton extends CollectorRegistry {
    public CollectorRegistrySingleton() {
        super(true);
    }
}
