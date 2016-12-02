/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.testutils;

import com.google.inject.AbstractModule;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;
import org.opendaylight.infrautils.caches.baseimpl.internal.CacheManagersRegistryImpl;
import org.opendaylight.infrautils.caches.ops.CacheManagers;
import org.opendaylight.infrautils.caches.standard.StandardCacheProvider;
import org.ops4j.pax.cdi.api.OsgiService;

/**
 * Guice Module for tests requiring a CacheProvider.
 *
 * @author Michael Vorburger.ch
 */
public class CacheModule extends AbstractModule {

    private final Class<? extends CacheProvider> providerClass;

    public CacheModule() {
        this(StandardCacheProvider.class);
    }

    public CacheModule(Class<? extends CacheProvider> providerClass) {
        super();
        this.providerClass = providerClass;
    }

    @Override
    protected void configure() {
        bind(CacheProvider.class).to(providerClass);
        bind(CacheProvider.class).annotatedWith(OsgiService.class).to(providerClass);

        CacheManagersRegistry monitor = new CacheManagersRegistryImpl();
        bind(CacheManagersRegistry.class).toInstance(monitor);
        bind(CacheManagers.class).toInstance(monitor);
    }
}
