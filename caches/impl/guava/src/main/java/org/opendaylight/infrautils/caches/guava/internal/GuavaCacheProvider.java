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
import java.util.Objects;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.NonDistributedNonTransactionalCacheProvider;
import org.opendaylight.infrautils.caches.spi.DelegatingNullSafeCache;
import org.opendaylight.infrautils.caches.spi.DelegatingNullSafeCheckedCache;

/**
 * Googgle Guava {@link LoadingCache} implementation of cache factory.

 * @author Michael Vorburger.ch
 */
public class GuavaCacheProvider implements NonDistributedNonTransactionalCacheProvider {

    @Override
    public <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig) {
        return new DelegatingNullSafeCache<>(
            new CacheGuavaAdapter<>(cacheConfig,
                newCacheBuilder(cacheConfig).build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return cacheConfig.cacheFunction().apply(key);
                    }
                })));
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig) {
        return new DelegatingNullSafeCheckedCache<>(
            new CheckedCacheGuavaAdapter<>(cacheConfig,
                newCacheBuilder(cacheConfig).build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K key) throws Exception {
                        return cacheConfig.cacheFunction().apply(key);
                    }
                })));
    }

    private <K, V> CacheBuilder<K, V> newCacheBuilder(BaseCacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig");
        @SuppressWarnings("unchecked")
        // TODO support from(String) with CacheBuilderSpec for configuration from a String
        CacheBuilder<K, V> builder = (CacheBuilder<K, V>) CacheBuilder.newBuilder();
        // TODO configure it!!!
        return builder;
    }

}
