/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.LoadingCache;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.opendaylight.infrautils.caches.BaseCache;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CachePolicy;
import org.opendaylight.infrautils.caches.CacheStats;

/**
 * Base class for {@link CacheGuavaAdapter} and {@link CheckedCacheGuavaAdapter}.
 *
 * @author Michael Vorburger.ch
 */
abstract class GuavaBaseCacheAdapter<K, V> implements BaseCache<K,V>, CacheManager {

    private final BaseCacheConfig config;
    private final Function<CachePolicy, LoadingCache<K, V>> policyToCache;
    private final AtomicReference<Holder<K,V>> holder;

    protected GuavaBaseCacheAdapter(BaseCacheConfig config, CachePolicy initialPolicy,
            Function<CachePolicy, LoadingCache<K, V>> policyToCache) {
        super();
        this.config = config;
        this.policyToCache = policyToCache;
        holder = new AtomicReference<>(newHolder(initialPolicy));
    }

    private Holder<K,V> newHolder(CachePolicy initialPolicy) {
        return new Holder<>(initialPolicy, policyToCache);
    }

    @Override
    public void setPolicy(CachePolicy newPolicy) {
        holder.lazySet(newHolder(newPolicy));
    }

    protected final LoadingCache<K, V> guavaCache() {
        return holder.get().guavaCache;
    }

    // BaseCache's methods

    @Override
    public final void evict(K key) {
        guavaCache().invalidate(key);
    }

    @Override
    public final CacheManager getManager() {
        return this;
    }

    @Override
    public final void close() throws Exception {
        guavaCache().cleanUp();
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
        return holder.get().stats;
    }

    @Override
    public CachePolicy getPolicy() {
        return holder.get().policy;
    }

    @Override
    public void evictAll() {
        guavaCache().invalidateAll();
    }

    // Holder class to group 3 fields for thread safe replacement in 1 AtomicReference operation
    private static final class Holder<K, V> {
        private final CachePolicy policy;
        private final GuavaCacheStatsAdapter stats;
        private final com.google.common.cache.LoadingCache<K, V> guavaCache;

        Holder(CachePolicy policy, Function<CachePolicy, LoadingCache<K, V>> policyToCache) {
            super();
            this.policy = policy;
            this.guavaCache = policyToCache.apply(policy);
            this.stats = new GuavaCacheStatsAdapter(guavaCache);
        }
    }
}
