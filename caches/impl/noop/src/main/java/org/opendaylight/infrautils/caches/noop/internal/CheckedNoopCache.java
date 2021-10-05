/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.CheckedCacheFunction;

/**
 * No Operation ("NOOP") implementation of CheckedCache.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
final class CheckedNoopCache<K, V, E extends Exception> implements CheckedCache<K, V, E> {
    private final CheckedCacheConfig<K, V, E> config;
    private final CheckedCacheFunction<K, V, E> function;
    private final CacheManager cacheManager;

    CheckedNoopCache(CheckedCacheConfig<K, V, E> config) {
        this.config = config;
        this.cacheManager = new NoopCacheManager(config);
        this.function = config.cacheFunction();
    }

    @Override
    public V get(K key) throws E {
        return function.get(key);
    }

    @Override
    public ImmutableMap<K, V> get(Iterable<? extends K> keys) throws E {
        return function.get(keys);
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        // Ignore!
    }

    @Override
    public void evict(K key) {
        return;
    }

    @Override
    public Map<K, V> asMap() {
        return Collections.emptyMap();
    }

    @Override
    public CacheManager getManager() {
        return cacheManager;
    }

    @Override
    public void close() {
        // Nothing to do.
    }

    @Override
    public String toString() {
        return "NoopCache{config=" + config + "}";
    }
}
