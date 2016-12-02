/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * Cache of keys to values, for cache function which may throw a checked exception.
 *
 * @see Cache for caches who's cache function never throw checked exceptions (i.e. only unchecked ones)
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

}
