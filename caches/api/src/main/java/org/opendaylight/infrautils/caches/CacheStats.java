/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Map;

/**
 * Statistics about a Cache.
 *
 * @author Michael Vorburger.ch
 */
public interface CacheStats {

    // Following are strongly typed getter for stats which all cache implementations will be able to provide

    long estimatedCurrentEntries();

    long missCount();

    long hitCount();

    /**
     * Extensions provide implementation specific cache stats.
     * This is good enough for e.g. a CLI to dump and display to end-users.
     */
    Map<String,Number> extensions();

}
