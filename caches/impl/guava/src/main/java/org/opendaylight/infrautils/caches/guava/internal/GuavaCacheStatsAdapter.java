/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.guava.internal;

import org.opendaylight.infrautils.caches.ops.CacheStats;

/**
 * Adapts CacheStats to Guava.
 *
 * @author Michael Vorburger.ch
 */
final class GuavaCacheStatsAdapter implements CacheStats {

    private final com.google.common.cache.CacheStats guavaStats;

    GuavaCacheStatsAdapter(com.google.common.cache.CacheStats guavaStats) {
        this.guavaStats = guavaStats;
    }

    @Override
    public long estimatedCurrentEntries() {
        return 0;
    }

    // TODO implement extensions() by accessing guavaStats ...

}
