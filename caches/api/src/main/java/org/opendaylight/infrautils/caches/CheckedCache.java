/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Arrays;
import java.util.Map;

/**
 * Cache of keys to values, for cache function which may throw a checked exception.
 *
 * <p>See {@link Cache} for caches who's cache function never throw checked exceptions (i.e. only unchecked ones).
 * Cache also has more general documentation which applies to this API as well.
 *
 * @param <K> key type of cache (must be immutable)
 * @param <V> value type of cache (should, for monitoring, typically, be of "similar" size for all keys)
 * @param <E> exception type cache can throw
 *
 * @author Michael Vorburger.ch
 */
public interface CheckedCache<K, V, E extends Exception> extends BaseCache<K,V> {

    /**
     * Get cache entry.
     *
     * @throws E if the cache's function throw that exception for this key
     *
     * @see {@link Cache#get(Object)} for more documentation.
     */
    V get(K key) throws E;

    Map<K, V> get(Iterable<? extends K> keys) throws E;

    @SuppressWarnings("unchecked")
    default Map<K, V> get(K... keys) throws E {
        return get(Arrays.asList(keys));
    }

}
