/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.micrometer.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;

/**
 * Micrometer {@link CompositeMeterRegistry} OSGi service singleton.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = MeterRegistry.class) // don't expose CompositeMeterRegistry
public class CompositeMeterRegistrySingleton extends CompositeMeterRegistry {

    @Override
    @PreDestroy
    public void close() {
        this.close();
    }

}
