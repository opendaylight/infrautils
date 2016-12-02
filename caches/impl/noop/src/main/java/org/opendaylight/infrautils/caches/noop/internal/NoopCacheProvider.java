/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import java.util.Objects;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;

/**
 * No Operation ("NOOP") implementation of cache factory.
 *
 * @author Michael Vorburger.ch
 */
public class NoopCacheProvider implements CacheProvider {

    @Override
    public <K, V> Cache<K, V> createNonDistributedNonTransactionalCache(CacheConfig<K, V> cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig");
        return new NoopCache<>(cacheConfig);
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E>
        createNonDistributedNonTransactionalCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig) {

        Objects.requireNonNull(cacheConfig, "cacheConfig");
        return new CheckedNoopCache<>(cacheConfig);
    }

}
