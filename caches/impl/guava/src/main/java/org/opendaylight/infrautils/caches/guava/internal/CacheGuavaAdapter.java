/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;

/**
 * Adapts {@link Cache} to Guava.
 *
 * @author Michael Vorburger.ch
 */
class CacheGuavaAdapter<K, V> extends AbstractGuavaCacheAdapter<K, V> implements Cache<K, V> {

    CacheGuavaAdapter(CacheConfig<K, V> config, com.google.common.cache.LoadingCache<K, V> guavaCache) {
        super(config, guavaCache);
    }

    @Override
    public V get(K key) {
        try {
            return guavaCache.getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(e.getMessage());
        }
    }

}
