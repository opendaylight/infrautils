/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.baseimpl.AbstractProvider;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;
import org.opendaylight.infrautils.caches.baseimpl.DelegatingNullSafeCache;
import org.opendaylight.infrautils.caches.baseimpl.DelegatingNullSafeCheckedCache;
import org.opendaylight.infrautils.caches.ops.CachePolicy;

/**
 * Googgle Guava {@link LoadingCache} implementation of cache factory.

 * @author Michael Vorburger.ch
 */
public class GuavaCacheProvider extends AbstractProvider {

    @Inject
    public GuavaCacheProvider(CacheManagersRegistry cachesMonitor) {
        super(cachesMonitor);
    }

    @Override
    public <K, V> Cache<K, V> newUnregisteredCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy) {
        return new DelegatingNullSafeCache<>(
            new CacheGuavaAdapter<>(cacheConfig, initialPolicy, policy ->
                newCacheBuilder(cacheConfig, policy).build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return cacheConfig.cacheFunction().get(key);
                    }

                    @Override
                    public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
                        return cacheConfig.cacheFunction().get(keys);
                    }
                })));
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E> newUnregisteredCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy) {
        return new DelegatingNullSafeCheckedCache<>(
            new CheckedCacheGuavaAdapter<>(cacheConfig, initialPolicy, policy ->
                newCacheBuilder(cacheConfig, policy).build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return cacheConfig.cacheFunction().get(key);
                    }

                    @Override
                    public Map<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
                        return cacheConfig.cacheFunction().get(keys);
                    }
                })));
    }

    private <K, V> CacheBuilder<K, V> newCacheBuilder(BaseCacheConfig cacheConfig, CachePolicy initialPolicy) {
        Objects.requireNonNull(cacheConfig, "cacheConfig");
        @SuppressWarnings("unchecked")
        // TODO support from(String) with CacheBuilderSpec for configuration from CachePolicy's extensions
        CacheBuilder<K, V> builder = (CacheBuilder<K, V>) CacheBuilder.newBuilder();
        // builder.concurrencyLevel(concurrencyLevel) <= this is a Guava specific setting, TBD String CacheBuilderSpec
        if (initialPolicy.statsEnabled()) {
            builder.recordStats();
        }
        builder.maximumSize(initialPolicy.maxEntries());
        // TODO builder.concurrencyLevel(concurrencyLevel);
        // TODO builder.initialCapacity(initialCapacity)
        return builder;
    }

}
