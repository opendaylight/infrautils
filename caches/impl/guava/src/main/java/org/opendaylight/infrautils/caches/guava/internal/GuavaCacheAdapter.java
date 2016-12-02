/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.opendaylight.infrautils.caches.Cache;

/**
 * Adapts {@link Cache} to Guava.
 *
 * @author Michael Vorburger.ch
 */
public class GuavaCacheAdapter<K, V> implements Cache<K, V> {

    private final com.google.common.cache.LoadingCache<K, V> guavaCache;

    public GuavaCacheAdapter(com.google.common.cache.LoadingCache<K, V> guavaCache) {
        this.guavaCache = guavaCache;
    }

    @Override
    public V get(K key) {
        try {
            return guavaCache.getUnchecked(key);
        } catch (UncheckedExecutionException | ExecutionError e) {
            // ? LOGGER.error("(..something..) failed", e); // TODO
        }
    }

    @Override
    public void evict(K key) {
        guavaCache.invalidate(key);
    }

    @Override
    public void close() throws Exception {
        guavaCache.cleanUp();
    }

}
