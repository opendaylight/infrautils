/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.CachePolicy;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfig;

/**
 * Adapts {@link CheckedCache} to Guava.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
final class CheckedCacheGuavaAdapter<K, V, E extends Exception>
    extends GuavaBaseCacheAdapter<K, V>
        implements CheckedCache<K, V, E> {

    CheckedCacheGuavaAdapter(CheckedCacheConfig<K, V, E> config, CachePolicy initialPolicy,
            Function<CachePolicy, LoadingCache<K, V>> policyToCache) {
        super(config, initialPolicy, policyToCache);
    }

    @Override
    // Suppress CS because propagating getCause() is what we want
    @SuppressWarnings({"unchecked", "checkstyle:AvoidHidingCauseException"})
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public V get(K key) throws E {
        try {
            return guavaCache().get(key);
        } catch (ExecutionException e) {
            throw (E) e.getCause();
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(
                    "InvalidCacheLoadException from Guava get(): " + e.getMessage(), e);
        }
    }

    @Override
    // Suppress CS because propagating getCause() is what we want
    @SuppressWarnings({"unchecked", "checkstyle:AvoidHidingCauseException"})
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public ImmutableMap<K, V> get(Iterable<? extends K> keys) throws E {
        try {
            return guavaCache().getAll(keys);
        } catch (ExecutionException e) {
            throw (E) e.getCause();
        } catch (InvalidCacheLoadException e) {
            throw new BadCacheFunctionRuntimeException(
                    "InvalidCacheLoadException from Guava getAll(): " + e.getMessage(), e);
        }
    }

}
