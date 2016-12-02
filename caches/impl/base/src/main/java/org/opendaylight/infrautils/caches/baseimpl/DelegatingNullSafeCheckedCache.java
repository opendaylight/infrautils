/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.ops.CacheManager;

/**
 * Cache with null handling, useful for API implementors (not users).
 *
 * @author Michael Vorburger.ch
 */
public final class DelegatingNullSafeCheckedCache<K,V,E extends Exception> implements CheckedCache<K,V,E> {

    private final CheckedCache<K,V,E> delegate;

    public DelegatingNullSafeCheckedCache(CheckedCache<K,V,E> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public V get(K key) throws BadCacheFunctionRuntimeException, E {
        Objects.requireNonNull(key, "null key (not supported)");
        V value = delegate.get(key);
        if (value == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value for key: " + key);
        }
        return value;
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) throws E {
        Objects.requireNonNull(keys, "null keys (not supported)");
        for (K key : keys) {
            Objects.requireNonNull(key, "null key in keys (not supported)");
        }
        Map<K, V> map = delegate.get(keys);
        if (map == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value instead of Map");
        }
        for (V value : map.values()) {
            if (value == null) {
                throw new BadCacheFunctionRuntimeException("Cache's function returned a null value");
            }
        }
        return map;
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
