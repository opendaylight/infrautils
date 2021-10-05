/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;

/**
 * {@link CheckedCache}'s Function, can throw checked Exception.
 *
 * <p>See also {@link CacheFunction}.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
@FunctionalInterface
public interface CheckedCacheFunction<K, V, E extends Exception> {

    /**
     * Calculate the value of the cache entry for the given key.
     * @param key the key for which to "calculate" (lookup, remote call, ...) a value.
     * @return value for the given key
     * @throws E checked exception thrown if no value for the given key could be obtained
     */
    V get(K key) throws E;

    /**
     * See {@link CacheFunction#get(Iterable)}.
     */
    default ImmutableMap<K, V> get(Iterable<? extends K> keys) throws E {
        ImmutableMap.Builder<K, V> mapBuilder = ImmutableMap.builder();
        for (K key : keys) {
            mapBuilder.put(key, get(key));
        }
        return mapBuilder.build();
    }

    /**
     * See {@link CacheFunction#from(Function)}.
     */
    default CacheFunction<K, V> from(Function<K, V> function) throws E {
        return function::apply;
    }

}
