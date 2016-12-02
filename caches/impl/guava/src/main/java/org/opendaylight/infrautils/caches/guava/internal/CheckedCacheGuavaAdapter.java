/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;
import org.opendaylight.infrautils.caches.ops.CachePolicy;

/**
 * Adapts {@link CheckedCache} to Guava.
 *
 * @author Michael Vorburger.ch
 */
final class CheckedCacheGuavaAdapter<K, V, E extends Exception>
    extends GuavaBaseCacheAdapter<K, V>
        implements CheckedCache<K, V, E> {

    CheckedCacheGuavaAdapter(CheckedCacheConfig<K, V, E> config, CachePolicy initialPolicy,
            com.google.common.cache.LoadingCache<K, V> guavaCache) {
        super(config, initialPolicy, guavaCache);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public V get(K key) throws E {
        try {
            return guavaCache.get(key);
        } catch (ExecutionException e) {
            throw (E) e.getCause();
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public Map<K, V> get(Iterable<? extends K> keys) throws E {
        try {
            return guavaCache.getAll(keys);
        } catch (ExecutionException e) {
            throw (E) e.getCause();
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(e.getMessage());
        }
    }

}
