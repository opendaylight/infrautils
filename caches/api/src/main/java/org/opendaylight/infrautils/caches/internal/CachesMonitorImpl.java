/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.spi.CachesMonitorSPI;

/**
 * Implementation of CachesMonitor.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public class CachesMonitorImpl implements CachesMonitorSPI {

    private final List<CacheManager> managers = new CopyOnWriteArrayList<>();
    private final List<CacheManager> unmodifiableManagers = Collections.unmodifiableList(managers);

    @Override
    public List<CacheManager> getAllCachesManagers() {
        return unmodifiableManagers;
    }

    @Override
    public void registerCacheManager(CacheManager cacheManager) {
        managers.add(cacheManager);
    }

}
