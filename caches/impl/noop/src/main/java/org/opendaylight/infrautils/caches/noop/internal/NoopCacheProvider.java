/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import java.util.Objects;
import javax.inject.Inject;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.ops.CachePolicy;
import org.opendaylight.infrautils.caches.spi.AbstractProvider;
import org.opendaylight.infrautils.caches.spi.CachesMonitorSPI;
import org.opendaylight.infrautils.caches.spi.DelegatingNullSafeCache;
import org.opendaylight.infrautils.caches.spi.DelegatingNullSafeCheckedCache;

/**
 * No Operation ("NOOP") implementation of cache factory.
 *
 * @author Michael Vorburger.ch
 */
public class NoopCacheProvider extends AbstractProvider {

    @Inject
    public NoopCacheProvider(CachesMonitorSPI cachesMonitor) {
        super(cachesMonitor);
    }

    @Override
    public <K, V> Cache<K, V> newUnregisteredCache(CacheConfig<K, V> cacheConfig, CachePolicy cachePolicy) {
        Objects.requireNonNull(cacheConfig, "cacheConfig");
        return new DelegatingNullSafeCache<>(new NoopCache<>(cacheConfig));
    }

    @Override
    public <K, V, E extends Exception> CheckedCache<K, V, E>
        newUnregisteredCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy cachePolicy) {

        Objects.requireNonNull(cacheConfig, "cacheConfig");
        return new DelegatingNullSafeCheckedCache<>(new CheckedNoopCache<>(cacheConfig));
    }

}
