/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.infrautils.caches.ops.CacheStats;

/**
 * Adapts CacheStats to Guava.
 *
 * @author Michael Vorburger.ch
 */
final class GuavaCacheStatsAdapter implements CacheStats {

    private final Cache<?, ?> guavaCache;

    GuavaCacheStatsAdapter(Cache<?, ?> guavaCache) {
        this.guavaCache = guavaCache;
    }

    @Override
    public long estimatedCurrentEntries() {
        return guavaCache.size();
    }

    @Override
    public long missCount() {
        return guavaCache.stats().missCount();
    }

    @Override
    public long hitCount() {
        return guavaCache.stats().hitCount();
    }

    @Override
    public Map<String, Number> extensions() {
        com.google.common.cache.CacheStats guavaStats = guavaCache.stats();
        return ImmutableMap.<String, Number>builder()
                .put("averageLoadPenalty", guavaStats.averageLoadPenalty())
                .put("evictionCount", guavaStats.evictionCount())
                .put("hitRate", guavaStats.hitRate())
                .put("loadCount", guavaStats.loadCount())
                .put("loadExceptionCount", guavaStats.loadExceptionCount())
                .put("loadExceptionRate", guavaStats.loadExceptionRate())
                .put("loadSuccessCount", guavaStats.loadSuccessCount())
                .put("missRate", guavaStats.missRate())
                .put("requestCount", guavaStats.requestCount())
                .put("totalLoadTime", guavaStats.totalLoadTime())
                .build();
    }

}
