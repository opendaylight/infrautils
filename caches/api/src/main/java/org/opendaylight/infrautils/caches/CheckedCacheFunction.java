/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.opendaylight.infrautils.caches.internal.CacheImplUtils;

/**
 * {@link CheckedCache}'s Function, can throw checked Exception.
 *
 * <p>See also {@link CacheFunction}.
 *
 * @author Michael Vorburger.ch
 */
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
    default Map<K, V> get(Iterable<? extends K> keys) throws E {
        Map<K, V> map = CacheImplUtils.newLinkedHashMapWithExpectedSize(keys);
        for (K key : keys) {
            map.put(key, get(key));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * See {@link CacheFunction#from(Function)}.
     */
    default CacheFunction<K, V> from(Function<K, V> function) throws E {
        return key -> function.apply(key);
    }

}
