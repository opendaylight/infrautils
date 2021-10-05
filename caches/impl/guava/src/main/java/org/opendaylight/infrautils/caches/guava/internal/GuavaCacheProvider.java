/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 * Copyright (c) 2020 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CachePolicy;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.baseimpl.AbstractProvider;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;
import org.opendaylight.infrautils.caches.baseimpl.DelegatingNullSafeCache;
import org.opendaylight.infrautils.caches.baseimpl.DelegatingNullSafeCheckedCache;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Google Guava {@link LoadingCache} implementation of cache factory.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
@Singleton
@Component(service = CacheProvider.class)
@RequireServiceComponentRuntime
public class GuavaCacheProvider extends AbstractProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GuavaCacheProvider.class);

    @Inject
    @Activate
    public GuavaCacheProvider(@Reference CacheManagersRegistry cachesMonitor) {
        super(cachesMonitor);
        LOG.info("Guava cache provider activated");
    }

    @Override
    @SuppressWarnings("resource")
    public <K, V> Cache<K, V> newUnregisteredCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy) {
        return new DelegatingNullSafeCache<>(new CacheGuavaAdapter<>(cacheConfig, initialPolicy,
            policy -> newCacheBuilder(cacheConfig, policy).build(new CacheLoader<K, V>() {
                @Override
                public V load(K key) {
                    return cacheConfig.cacheFunction().get(key);
                }

                @Override
                public ImmutableMap<K, V> loadAll(Iterable<? extends K> keys) {
                    return cacheConfig.cacheFunction().get(keys);
                }
            })));
    }

    @Override
    @SuppressWarnings("resource")
    public <K, V, E extends Exception> CheckedCache<K, V, E> newUnregisteredCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy) {
        return new DelegatingNullSafeCheckedCache<>(new CheckedCacheGuavaAdapter<>(cacheConfig, initialPolicy,
            policy -> newCacheBuilder(cacheConfig, policy).build(new CacheLoader<K, V>() {
                @Override
                public V load(K key) throws Exception {
                    return cacheConfig.cacheFunction().get(key);
                }

                @Override
                public ImmutableMap<K, V> loadAll(Iterable<? extends K> keys) throws Exception {
                    return cacheConfig.cacheFunction().get(keys);
                }
            })));
    }

    @Deactivate
    void deactivate() {
        LOG.info("Guava cache provider deactivated");
    }

    private static <K, V> CacheBuilder<K, V> newCacheBuilder(BaseCacheConfig cacheConfig, CachePolicy initialPolicy) {
        Objects.requireNonNull(cacheConfig, "cacheConfig");
        @SuppressWarnings("unchecked")
        // TODO support from(String) with CacheBuilderSpec for configuration from CachePolicy's extensions
        CacheBuilder<K, V> builder = (CacheBuilder<K, V>) CacheBuilder.newBuilder();
        // builder.concurrencyLevel(concurrencyLevel) <= this is a Guava specific setting, TBD String CacheBuilderSpec
        if (initialPolicy.statsEnabled()) {
            builder.recordStats();
        }

        long maxEntries = initialPolicy.maxEntries();
        if (maxEntries != CachePolicy.UNLIMITED_ENTRIES) {
            builder.maximumSize(maxEntries);
        }

        // TODO @see CachePolicy
        //   * TODO builder.concurrencyLevel(initialPolicy.?.concurrencyLevel);
        //   * TODO builder.initialCapacity(initialPolicy.?.initialCapacity)
        return builder;
    }

}
