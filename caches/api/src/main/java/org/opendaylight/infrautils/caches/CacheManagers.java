/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Service to monitor all registered known caches.
 *
 * <p>Used by e.g. CLI commands, web UIs, etc.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface CacheManagers {

    /**
     * Get the {@link CacheManager} for all caches.
     */
    Iterable<CacheManager> getAllCacheManagers();

    /**
     * Get the {@link CacheManager} for a particular cache, via its ID.
     */
    CacheManager getCacheManager(String cacheID) throws IllegalArgumentException;

}
