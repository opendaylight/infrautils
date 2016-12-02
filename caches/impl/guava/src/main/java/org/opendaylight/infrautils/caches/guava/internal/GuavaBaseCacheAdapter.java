/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.LoadingCache;
import org.opendaylight.infrautils.caches.BaseCache;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.ops.CachePolicy;
import org.opendaylight.infrautils.caches.ops.CacheStats;

/**
 * Base class for {@link CacheGuavaAdapter} and {@link CheckedCacheGuavaAdapter}.
 *
 * @author Michael Vorburger.ch
 */
abstract class GuavaBaseCacheAdapter<K, V> implements BaseCache<K,V>, CacheManager {

    private final CachePolicy policy;
    private final BaseCacheConfig config;
    private final GuavaCacheStatsAdapter stats;

    protected final com.google.common.cache.LoadingCache<K, V> guavaCache;

    protected GuavaBaseCacheAdapter(BaseCacheConfig config, CachePolicy initialPolicy, LoadingCache<K, V> guavaCache) {
        super();
        this.config = config;
        this.policy = initialPolicy;
        this.guavaCache = guavaCache;
        this.stats = new GuavaCacheStatsAdapter(guavaCache.stats());
    }

    // BaseCache's methods

    @Override
    public final void evict(K key) {
        guavaCache.invalidate(key);
    }

    @Override
    public CacheManager getManager() {
        return this;
    }

    @Override
    public final void close() throws Exception {
        guavaCache.cleanUp();
    }

    @Override
    public final String toString() {
        return "NoopCache{config=" + config + "}"; // TODO print stats & stuff from guavaCache
    }

    // CacheManager's methods

    @Override
    public BaseCacheConfig getConfig() {
        return config;
    }

    @Override
    public CacheStats getStats() {
        return stats;
    }

    @Override
    public CachePolicy getPolicy() {
        return policy;
    }

    @Override
    public void setPolicy(CachePolicy newPolicy) {
        // TODO implement ... needs an outside/inside wrapper kinda thing
    }

    @Override
    public void evictAll() {
        guavaCache.invalidateAll();
    }

}
