/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.standard;

import javax.inject.Singleton;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;
import org.opendaylight.infrautils.caches.baseimpl.internal.CacheManagersRegistryImpl;
import org.opendaylight.infrautils.caches.guava.internal.GuavaCacheProvider;
import org.opendaylight.infrautils.caches.ops.CachePolicy;

/**
 * Default standard usual ordinary habitual ;) {@link CacheProvider}. Use this
 * for example when writing an end2end component test and wanting to bind to "just some
 * implementation, the usual one that will probably be available at runtime".
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class StandardCacheProvider implements CacheProvider {

    private final CacheProvider delegate;

    public StandardCacheProvider() {
        this(new CacheManagersRegistryImpl());
    }

    public StandardCacheProvider(CacheManagersRegistry cacheManagersRegistry) {
        // currently hard-coded to Guava implementaiton, but this may change...
        delegate = new GuavaCacheProvider(cacheManagersRegistry);
    }

    @Override
    public <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy) {
        return delegate.newCache(cacheConfig, initialPolicy);
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy) {
        return delegate.newCheckedCache(cacheConfig, initialPolicy);
    }

}
