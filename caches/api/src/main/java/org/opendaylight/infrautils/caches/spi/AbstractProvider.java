/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.spi;

import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.ops.CachePolicy;

/**
 * Base class of provider, useful for API implementors (not users).
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractProvider implements CacheProvider {

    private final CacheManagersRegistry cachesMonitor;

    protected AbstractProvider(CacheManagersRegistry cachesMonitor) {
        this.cachesMonitor = cachesMonitor;
    }

    protected abstract <K, V> Cache<K, V> newUnregisteredCache(CacheConfig<K,V> cacheConfig, CachePolicy initialPolicy);

    protected abstract <K, V, E extends Exception> CheckedCache<K, V, E> newUnregisteredCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy);

    @Override
    public final <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy) {
        Cache<K, V> cache = newUnregisteredCache(cacheConfig, initialPolicy);
        cachesMonitor.registerCacheManager(cache.getManager());
        return cache;
    }

    @Override
    public final <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy) {
        CheckedCache<K, V, E> cache = newUnregisteredCheckedCache(cacheConfig, initialPolicy);
        cachesMonitor.registerCacheManager(cache.getManager());
        return cache;
    }

}
