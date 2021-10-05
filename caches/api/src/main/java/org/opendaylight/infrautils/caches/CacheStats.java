/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import com.google.common.collect.ImmutableMap;

/**
 * Statistics about a Cache.
 *
 * <p>The data returned by this interface may not be 100% accurate, due to concurrency optimizations in implementations.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
public interface CacheStats {

    // Following are strongly typed getter for stats which all cache implementations will be able to provide

    /**
     * Number of key/value pair entries currently in the cache.
     */
    long estimatedCurrentEntries();

    /**
     * Number of "misses" in the cache.
     * A "miss" causes the {@link CacheFunction} to have to be invoked on a {@link Cache#get(Object)}.
     */
    long missCount();

    /**
     * Number of "hits" in the cache. A "hit" is when a Cache can return from a
     * {@link Cache#get(Object)} without having to use its {@link CacheFunction}.
     */
    long hitCount();

    /**
     * Extensions provide implementation specific cache stats.
     * This is good enough for e.g. a CLI to dump and display to end-users.
     */
    ImmutableMap<String,Number> extensions();

}
