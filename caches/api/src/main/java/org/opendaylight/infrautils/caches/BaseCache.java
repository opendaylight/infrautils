/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import org.opendaylight.infrautils.caches.ops.CacheManager;

/**
 * Base class for {@link Cache} and {@link CheckedCache}.
 *
 * @author Michael Vorburger.ch
 */
public interface BaseCache<K,V> extends AutoCloseable {

    /**
     * Evict an entry from the cache.
     * If the cache does not currently contain an entry under this key,
     * then this is ignored.  If it does, that entry is evicted, to be
     * re-calculated on the next get.
     */
    void evict(K key);

    // We intentionally do *NOT* provide a replace(K key, V value) method,
    // as this could lead to cache inconsistencies if badly used (and people will...);
    // the *ONLY* way to get values *MUST* be through the cache function!

    CacheManager getManager();

}
