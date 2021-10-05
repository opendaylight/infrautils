/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl;

import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.CacheProvider;

/**
 * Service Provider (Cache implementor) hook. Implementations of this interface are expected to be thread-safe.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
public interface CacheManagersRegistry extends CacheManagers {

    // TODO see if by using AbstractProvider it may be possible to avoid this IF altogether ?

    /**
     * Implementations of {@link CacheProvider}
     * invoke this for each new {@link CacheManager} they're about to return to
     * users.
     */
    void registerCacheManager(CacheManager cacheManager);

}
