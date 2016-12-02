/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.ops.CachePolicy;
import org.opendaylight.infrautils.caches.ops.CacheStats;

/**
 * No Operation ("NOOP") implementation of CacheManager.
 *
 * @author Michael Vorburger.ch
 */
class NoopCacheManager implements CacheManager {

    private final BaseCacheConfig config;

    NoopCacheManager(BaseCacheConfig config) {
        this.config = config;
    }

    @Override
    public BaseCacheConfig getConfig() {
        return config;
    }

    @Override
    public CacheStats getStats() {
        return NoopCacheStats.INSTANCE;
    }

    @Override
    public CachePolicy getPolicy() {
        return NoopCachePolicy.INSTANCE;
    }

    @Override
    public void setPolicy(CachePolicy newPolicy) {
        // Ignored.
    }

    @Override
    public void evictAll() {
        // Ignored.
    }

}
