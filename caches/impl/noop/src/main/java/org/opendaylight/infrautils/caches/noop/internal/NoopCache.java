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
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CacheFunction;
import org.opendaylight.infrautils.caches.CacheManager;

/**
 * No Operation ("NOOP") implementation of Cache.
 *
 * @author Michael Vorburger.ch
 */
final class NoopCache<K, V> implements Cache<K, V> {
    private final CacheConfig<K, V> config;
    private final CacheFunction<K, V> function;
    private final CacheManager manager;

    NoopCache(CacheConfig<K, V> config) {
        this.config = config;
        this.manager = new NoopCacheManager(config);
        this.function = config.cacheFunction();
    }

    @Override
    public V get(K key) {
        return function.get(key);
    }

    @Override
    public ImmutableMap<K, V> get(Iterable<? extends K> keys) {
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
        return manager;
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
