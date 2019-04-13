/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * Manage a Cache.
 *
 * <p>Allows to change policy settings at run-time.
 *
 * <p>Intentionally does <b>NOT</b> give direct programmatic access to the {@link Cache}.
 *
 * <p>Implementations of this interface are expected to be thread-safe.
 *
 * @author Michael Vorburger.ch
 */
public interface CacheManager {

    /**
     * Get this Cache's (fixed) configuration.
     */
    BaseCacheConfig getConfig();

    /**
     * Get this Cache's latest statistics.
     * This may return dummy/empty 0 stats, if the the Cache does not have {@link CachePolicy#statsEnabled()}.
     */
    CacheStats getStats();

    /**
     * Get this Cache's current policy.
     */
    CachePolicy getPolicy();

    /**
     * Update this Cache's policy.
     */
    void setPolicy(CachePolicy newPolicy);

    /**
     * Evict <b>ALL</b> entries from this Cache.
     *
     * <p>Use {@link BaseCache#evict(Object)} to evict a single entry.
     */
    void evictAll();

}
