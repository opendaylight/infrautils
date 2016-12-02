/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests.impl;

import org.opendaylight.infrautils.caches.NonDistributedNonTransactionalCacheProvider;
import org.opendaylight.infrautils.caches.internal.CachesMonitorImpl;
import org.opendaylight.infrautils.caches.ops.CachesMonitor;
import org.opendaylight.infrautils.caches.spi.CachesMonitorSPI;
import org.opendaylight.infrautils.inject.guice.testutils.AbstractGuiceJsr250Module;

class TestModule extends AbstractGuiceJsr250Module {

    private final Class<? extends NonDistributedNonTransactionalCacheProvider> providerClass;

    TestModule(Class<? extends NonDistributedNonTransactionalCacheProvider> providerClass) {
        super();
        this.providerClass = providerClass;
    }

    @Override
    protected void configureBindings() {
        bind(NonDistributedNonTransactionalCacheProvider.class).to(providerClass);

        CachesMonitorSPI monitor = new CachesMonitorImpl();
        bind(CachesMonitorSPI.class).toInstance(monitor);
        bind(CachesMonitor.class).toInstance(monitor);
    }
}
