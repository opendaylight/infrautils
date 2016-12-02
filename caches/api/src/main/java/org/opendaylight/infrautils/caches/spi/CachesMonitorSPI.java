/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.spi;

import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.ops.CachesMonitor;

/**
 * Service Provider (Cache implementor) hook.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface CachesMonitorSPI extends CachesMonitor {

    // TODO see if by using AbstractProvider it may be possible to avoid this IF altogether ?

    /**
     * Implementations of {@link CacheProvider}
     * invoke this for each new {@link CacheManager} they're about to return to
     * users.
     */
    void registerCacheManager(CacheManager cacheManager);

}
