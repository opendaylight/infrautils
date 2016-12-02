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
 * {@link Cache}'s Function.
 *
 * <p>Use this if your implementation throws no exception (i.e. unly unchecked
 * runtime exceptions). If your cache function throws checked exceptions, then
 * just use a {@link CheckedCacheFunction} instead.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CacheFunction<K, V> {

    V get(K key);

    /**
     * Implementations may wish to override this implementation
     * if they can provide bulk implementations which avoid 1-by-1
     * locking overhead which single get() may incur, which is what
     * the default implementation does.
     */
    default Map<K, V> get(Iterable<? extends K> keys) {
        // see also org.opendaylight.infrautils.caches.noop.internal.NoopCache.get(Iterable<? extends K>)
        Map<K, V> map = CacheImplUtils.newLinkedHashMapWithExpectedSize(keys);
        for (K key : keys) {
            map.put(key, get(key));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Convenience mapping from java.util.function.Function.
     * Typically never overridden.
     */
    default CacheFunction<K, V> from(Function<K, V> function) {
        return key -> function.apply(key);
    }

}
