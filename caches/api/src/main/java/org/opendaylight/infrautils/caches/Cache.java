/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Cache abstraction.
 *
 * <p>Users typically obtain implementations from the {@link CacheProvider}.
 *
 * <p>Implementations are typically backed by established cache frameworks,
 * such as Ehcache, Infinispan, Guava's, Caffeine, imcache, cache2k, etc.
 *
 * @param <K> key type of cache
 * @param <V> value type of cache
 * @param <E> exception type cache can throw
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface Cache<K,V, E extends Exception> extends AutoCloseable {

    // TODO study API of https://github.com/google/guava/wiki/CachesExplained

    // TODO study API of JSR 107 javax.cache (JCache) implementations such as <a href="http://infinispan.org">Infinispan</a> or <a href="http://www.ehcache.org">Ehcache</a>

    /**
     * Get cache entry.
     * If the cache does not currently contain an entry under this key,
     * then one is created, using the function given when the cache was
     * created.
     *
     * @param key key of cache entry
     * @return value, never null (but may be an Optional)
     */
    V get(K key) /* TODO ?? throws E */;

    /**
     * Evict an entry from the cache.
     * If the cache does not currently contain an entry under this key,
     * this this is ignored.  If it does, that entry is evicted, to be
     * re-calculated on the next get.
     */
    void evict(K key);

    /**
     * Replace a cache entry.
     * This is *NOT* a put; if this value was not already cached, then this does nothing.
     *
     * @param key key to replace in the cache
     * @param value value to replace in the cache
     */
    // Not needed? @return if key indeed was found in the cache, and replaced; false if the call was ignored
    void replace(K key, V value);

}
