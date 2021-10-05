/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.BaseCache;
import org.opendaylight.infrautils.caches.CacheManager;

@Deprecated(since = "2.0.7", forRemoval = true)
@Beta
public abstract class DelegatingNullSafeBaseCache<K, V> extends ForwardingObject implements BaseCache<K, V> {
    @Override
    protected abstract BaseCache<K, V> delegate();

    @Override
    public void evict(K key) {
        delegate().evict(requireNonNull(key, "null key (not supported)"));
    }

    @Override
    public void put(K key, V value) {
        delegate().put(requireNonNull(key, "key"), requireNonNull(value, "value"));
    }

    @Override
    public Map<K, V> asMap() {
        return delegate().asMap();
    }

    @Override
    public CacheManager getManager() {
        return delegate().getManager();
    }

    @Override
    public void close() throws Exception {
        delegate().close();
    }

    protected static <K> Iterable<K> checkNonNullKeys(@Nullable Iterable<K> keys) {
        for (K key : requireNonNull(keys, "null keys (not supported)")) {
            Objects.requireNonNull(key, "null key in keys (not supported)");
        }
        return keys;
    }

    protected static <K, V> V checkReturnValue(K key, @Nullable V value) {
        if (value == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value for key: " + key);
        }
        return value;
    }

    protected static <K, V> ImmutableMap<K, V> checkReturnValue(@Nullable ImmutableMap<K, V> map) {
        if (map == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value instead of Map");
        }
        return map;
    }
}
