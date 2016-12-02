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
 * Cache of keys to values.
 *
 * <p>Users typically obtain implementations from the {@link NonDistributedNonTransactionalCacheProvider}.
 *
 * <p>Implementations are typically backed by established cache frameworks,
 * such as Ehcache, Infinispan, Guava's, Caffeine, imcache, cache2k, etc.
 *
 * @param <K> key type of cache (must be javax.annotation.concurrent.Immutable)
 * @param <V> value type of cache
 * @param <E> exception type cache can throw
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface Cache<K,V> extends BaseCache<K,V> {

    /**
     * Get cache entry.
     * If the cache does not currently contain an entry under this key,
     * then one is created, using the function given when the cache was
     * created.
     *
     * @param key key of cache entry
     * @return value, never null (but may be an Optional)
     * @throws BadCacheFunctionRuntimeException if the cache's function returned null value
     * @throws NullPointerException if the cache's users passed a null key
     */
    V get(K key);

}
