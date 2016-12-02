/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.spi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.ops.CacheManager;

/**
 * Cache with null handling, useful for API implementors (not users).
 *
 * @author Michael Vorburger.ch
 */
public final class DelegatingNullSafeCache<K,V> implements Cache<K,V> {

    private final Cache<K,V> delegate;

    public DelegatingNullSafeCache(Cache<K, V> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public V get(K key) {
        Objects.requireNonNull(key, "null key (not supported)");
        V value = delegate.get(key);
        if (value == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value for key: " + key);
        }
        return value;
    }

    @Override
    public void evict(K key) {
        Objects.requireNonNull(key, "null key (not supported)");
        delegate.evict(key);
    }

    @Override
    public CacheManager getManager() {
        return delegate.getManager();
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
