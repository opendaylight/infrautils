/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.CheckedFunction;
import org.opendaylight.infrautils.caches.ops.CacheManager;

/**
 * No Operation ("NOOP") implementation of CheckedCache.
 *
 * @author Michael Vorburger.ch
 */
final class CheckedNoopCache<K, V, E extends Exception> implements CheckedCache<K, V, E> {

    private final CheckedCacheConfig<K, V, E> config;
    private final CheckedFunction<K, V, E> function;
    private final CacheManager cacheManager;

    CheckedNoopCache(CheckedCacheConfig<K, V, E> config) {
        this.config = config;
        this.cacheManager = new NoopCacheManager(config);
        this.function = config.cacheFunction();
    }

    @Override
    public V get(K key) throws E {
        return function.apply(key);
    }

    @Override
    public void evict(K key) {
        return;
    }

    @Override
    public CacheManager getManager() {
        return cacheManager;
    }

    @Override
    public void close() throws Exception {
        // Nothing to do.
    }

    @Override
    public String toString() {
        return "NoopCache{config=" + config + "}";
    }

}
