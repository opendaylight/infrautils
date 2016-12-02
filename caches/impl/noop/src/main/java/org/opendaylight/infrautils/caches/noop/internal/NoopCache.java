/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import java.util.function.Function;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.spi.AbstractCache;

/**
 * No Operation ("NOOP") implementation of Cache.
 *
 * @author Michael Vorburger.ch
 */
public final class NoopCache<K, V> extends AbstractCache<K, V> {

    private final CacheConfig<K, V> config;
    private final Function<K, V> function;

    public NoopCache(CacheConfig<K, V> config) {
        this.config = config;
        this.function = config.cacheFunction();
    }

    @Override
    public V getNullable(K key) {
        return function.apply(key);
    }

    @Override
    public void evict(K key) {
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
