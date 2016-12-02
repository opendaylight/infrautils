/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.noop.internal;

import java.util.Collections;
import java.util.Map;
import org.opendaylight.infrautils.caches.CacheStats;

/**
 * No Operation ("NOOP") implementation of CacheStats.
 *
 * @author Michael Vorburger.ch
 */
final class NoopCacheStats implements CacheStats {

    static final CacheStats INSTANCE = new NoopCacheStats();

    private NoopCacheStats() { }

    @Override
    public long estimatedCurrentEntries() {
        return 0;
    }

    @Override
    public long missCount() {
        return 0;
    }

    @Override
    public long hitCount() {
        return 0;
    }

    @Override
    public Map<String, Number> extensions() {
        return Collections.emptyMap();
    }

}
