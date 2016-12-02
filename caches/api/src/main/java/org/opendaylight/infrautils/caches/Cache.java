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
import javax.annotation.concurrent.ThreadSafe;

/**
 * Cache of keys to values.
 *
 * <p>Users typically obtain implementations from the {@link CacheProvider}.
 *
 * <p><b>Caches are not {@link Map}s!</b> Differences include that a <i>Map persists
 * all elements that are added to it until they are explicitly removed. A Cache on the
 * other hand is generally configured to evict entries automatically, in order to constrain
 * its memory footprint</i>, based on some policy (quoted from
 * <a href="https://github.com/ben-manes/caffeine/wiki">Caffeine's</a> or
 * <a href="https://github.com/google/guava/wiki/CachesExplained">Guava's</a>
 * introduction).  Another notable difference, enforced by this caching API, is
 * that caches should <b>not</b> be thought of as data structures that you <i>put</i>
 * something in somewhere in your code to <i>get</i> it out of somewhere else.  Instead, a
 * Cache is "just a façade" to a {@link CacheFunction}'s <i>get</i>.  This design enforces
 * proper encapsulation, and helps you not to screw up the content of your cache (like you
 * easily can, and usually do, when you use a Map as a cache).
 *
 * <p>The implementation of this API is, typically, backed by established cache frameworks,
 * such as Ehcache, Infinispan, Guava's, Caffeine, ..., imcache, cache2k, ... etc.
 *
 * @param <K> key type of cache (must be immutable and have correct hashCode &amp; equals)
 * @param <V> value type of cache (should, for monitoring and predictable memory
 *            effect of eviction, typically, be of "similar" size for all keys;
 *            else consider simply using separate Cache, if feasible)
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface Cache<K,V> extends BaseCache<K,V> {

    /**
     * Get cache entry.
     * If the cache does not currently contain an entry under this key,
     * then one is created, using the {@link CacheFunction} given when the cache was
     * created.
     *
     * @param key key of cache entry
     * @return value, never null (but may be an Optional)
     * @throws BadCacheFunctionRuntimeException if the cache's function returned null value
     * @throws NullPointerException if the cache's users passed a null key
     */
    V get(K key);

    Map<K, V> get(Iterable<? extends K> keys);

    @SuppressWarnings("unchecked")
    default Map<K, V> get(K... keys) {
        return get(Arrays.asList(keys));
    }

}
