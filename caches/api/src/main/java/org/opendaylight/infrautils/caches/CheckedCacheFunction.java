/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.opendaylight.infrautils.caches.internal.CacheImplUtils;

/**
 * {@link CheckedCache}'s Function, can throw checked Exception.
 *
 * <p>See also {@link CacheFunction}.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedCacheFunction<K, V, E extends Exception> {

    V get(K key) throws E;

    default Map<K, V> get(Iterable<? extends K> keys) throws E {
        Map<K, V> map = CacheImplUtils.newLinkedHashMapWithExpectedSize(keys);
        for (K key : keys) {
            map.put(key, get(key));
        }
        return Collections.unmodifiableMap(map);
    }

    default CacheFunction<K, V> from(Function<K, V> function) throws E {
        return key -> function.apply(key);
    }

}
