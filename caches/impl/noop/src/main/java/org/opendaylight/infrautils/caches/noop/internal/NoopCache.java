/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import java.util.function.Function;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfig;

/**
 * No Operation ("NOOP") implementation of Cache.
 *
 * @author Michael Vorburger.ch
 */
public class NoopCache<K, V, E extends Exception> implements Cache<K, V, E> {

    private final CacheConfig<K, V, E> config;
    private final Function<K, V> function;

    public NoopCache(CacheConfig<K, V, E> config) {
        this.config = config;
        this.function = config.cacheFunction();
    }

    @Override
    public V get(K key) /* TODO throws E */ {
        return function.apply(key);
    }

    @Override
    public void evict(K key) {
        return;
    }

    @Override
    public void replace(K key, V value) {
        return;
    }

    @Override
    public void close() throws Exception {
        // Nothing to do.
    }

    @Override
    public String toString() {
        return "NoopCache{config=" + config + "}";
    }
}
