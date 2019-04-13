/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;

/**
 * {@link Cache}'s Function.
 *
 * <p>Use this if your implementation throws no exception (i.e. only unchecked
 * runtime exceptions). If your cache function throws checked exceptions, then
 * just use a {@link CheckedCacheFunction} instead.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CacheFunction<K, V> {

    /**
     * Calculate the value of the cache entry for the given key.
     * @param key the key for which to "calculate" (lookup, remote call, ...) a value.
     * @return value for the given key
     */
    @NonNull V get(@NonNull K key);

    /**
     * Implementations may wish to override this implementation
     * if they can provide bulk implementations which avoid 1-by-1
     * locking overhead which single get() may incur, which is what
     * the default implementation does.
     *
     * @param keys list of keys of cache entries
     * @return Map of cache keys and values (neither ever null, but may be an Optional)
     */
    default ImmutableMap<K, V> get(Iterable<? extends K> keys) {
        Builder<K, V> mapBuilder = ImmutableMap.builder();
        for (K key : keys) {
            mapBuilder.put(key, get(key));
        }
        return mapBuilder.build();
    }

    /**
     * Convenience mapping from java.util.function.Function.
     * Typically never overridden.
     */
    default CacheFunction<K, V> from(Function<K, V> function) {
        return function::apply;
    }

}
