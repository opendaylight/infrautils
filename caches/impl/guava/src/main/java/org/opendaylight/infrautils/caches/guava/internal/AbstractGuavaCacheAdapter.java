/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.LoadingCache;
import org.opendaylight.infrautils.caches.BaseCacheConfig;

/**
 * Base class for {@link CacheGuavaAdapter} and {@link CheckedCacheGuavaAdapter}.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractGuavaCacheAdapter<K, V> implements AutoCloseable {

    private final BaseCacheConfig config;
    protected final com.google.common.cache.LoadingCache<K, V> guavaCache;

    protected AbstractGuavaCacheAdapter(BaseCacheConfig config, LoadingCache<K, V> guavaCache) {
        super();
        this.config = config;
        this.guavaCache = guavaCache;
    }

    public final void evict(K key) {
        guavaCache.invalidate(key);
    }

    @Override
    public final void close() throws Exception {
        guavaCache.cleanUp();
    }

    @Override
    public final String toString() {
        return "NoopCache{config=" + config + "}"; // TODO print stats & stuff from guavaCache
    }

}
