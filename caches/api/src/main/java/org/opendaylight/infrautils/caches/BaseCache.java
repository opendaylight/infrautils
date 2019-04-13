/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base class for {@link Cache} and {@link CheckedCache}.
 *
 * @author Michael Vorburger.ch
 */
public interface BaseCache<K, V> extends AutoCloseable {

    /**
     * Evict an entry from the cache.
     * If the cache does not currently contain an entry under this key,
     * then this is ignored.  If it does, that entry is evicted, to be
     * re-calculated on the next get.
     *
     * <p>Use {@link CacheManager#evictAll()} to evict all entries of the entire cache.
     */
    void evict(K key);

    /**
     * Puts a new entry into the Cache, replacing any existing one.
     *
     * <p>Normally, you often never need to call this method, as in regular usage
     * scenarios a cache API client just invokes {@link Cache#get(Object)} (or {@link CheckedCache#get(Object)},
     * which then internally (may) use the {@link CacheFunction} (or {@link CheckedCacheFunction}) - without you
     * ever having had to explicitly <i>put</i> something into the Cache.
     *
     * <p>This method is <b>ONLY</b> (!) intended for "optimizations". It is useful if the code
     * using a cache already has a key and value (e.g. following an external update
     * notification event kind of thing) and wants to "save time" on an expected
     * subsequent {@link Cache#get(Object)} (or {@link CheckedCache#get(Object)}
     * invoking the {@link CacheFunction} (or {@link CheckedCacheFunction})
     * unnecessarily, using this "hint" to "propose" an entry to the cache.
     *
     * <p>Any code using this must expect key/value pairs that have been put into a
     * cache to disappear at any time (e.g. when the cache is full and this
     * key/value hasn't been used, or after a programmatic or end-user operator
     * initiated eviction), and be able to obtain <b>THE SAME</b> value from the
     * {@link CacheFunction} (or {@link CheckedCacheFunction}), for the given key.
     *
     * <p>Failure to implement calls to this put method consistent with the implementation
     * of the cache's get function <b>WILL</b> lead to weird cache inconsistencies!
     *
     * <p>Some Cache implementations may <b>IGNORE</b> this "hint" method.
     *
     * @param key the key of the proposed new cache entry
     * @param value the value of the proposed new cache entry
     * @throws NullPointerException if the cache's users passed a null key or value
     */
    void put(@NonNull K key, @NonNull V value);

    /**
     * Returns a view of the entries stored in this cache as an immutable map.
     */
    Map<K, V> asMap();

    CacheManager getManager();
}
