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
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.ops.CachePolicy;

/**
 * Adapts {@link Cache} to Guava.
 *
 * @author Michael Vorburger.ch
 */
final class CacheGuavaAdapter<K, V> extends GuavaBaseCacheAdapter<K, V> implements Cache<K, V> {

    CacheGuavaAdapter(CacheConfig<K, V> config, CachePolicy initialPolicy,
            Function<CachePolicy, LoadingCache<K, V>> policyToCache) {
        super(config, initialPolicy, policyToCache);
    }

    @Override
    public V get(K key) {
        try {
            return guavaCache().getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(e.getMessage());
        }
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        try {
            return guavaCache().getAll(keys);
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(e.getMessage());
        } catch (ExecutionException e) {
            // This normally should never happen here, because according to Guava Cache's doc,
            // an ExecutionException is thrown by getAll when the its CacheLoader (thus our
            // CacheFunction) throws a checked exception - but our CacheFunction never can,
            // according to its signature - that's what the CheckedCacheFunction and
            // CheckedCacheGuavaAdapter are for... if this happens, something is wrong.
            // NB This is very different in CheckedCacheGuavaAdapter, where we do expect it!
            throw new BadCacheFunctionRuntimeException("CacheFunction checked exception", e);
        }
    }

}
