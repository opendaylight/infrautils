/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * Cache.
 *
 * @author Michael Vorburger
 */
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
    V get(K key) throws E;

    boolean evict(K key);

    /**
     * Replace a cache entry.
     * This is *NOT* a put; if this value was not already cached, then this does nothing.
     *
     * @param key key to replace in the cache
     * @param value value to replace in the cache
     * @return if key indeed was found in the cache, and replaced; false if the call was ignored
     */
    boolean replace(K key, V value);

}
