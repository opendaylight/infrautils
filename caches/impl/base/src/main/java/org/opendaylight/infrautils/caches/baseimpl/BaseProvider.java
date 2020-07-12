/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl;

import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CachePolicyBuilder;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;

/**
 * Minimal implementation of CacheProvider.
 * See {@link AbstractProvider} for the abstract class which typical implementations should extend.
 *
 * @author Michael Vorburger.ch
 */
@Deprecated(forRemoval = true)
public abstract class BaseProvider implements CacheProvider {
    @Override
    public <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig) {
        return newCache(cacheConfig, new CachePolicyBuilder().build());
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig) {
        return newCheckedCache(cacheConfig, new CachePolicyBuilder().build());
    }
}
