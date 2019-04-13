/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.baseimpl.CacheManagersRegistry;

/**
 * Implementation of CachesMonitor. This class is thread-safe.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Service(classes = { CacheManagers.class, CacheManagersRegistry.class })
public class CacheManagersRegistryImpl implements CacheManagersRegistry {

    private final List<CacheManager> managers = new CopyOnWriteArrayList<>();
    private final List<CacheManager> unmodifiableManagers = Collections.unmodifiableList(managers);

    @Override
    public void registerCacheManager(CacheManager cacheManager) {
        managers.add(cacheManager);
    }

    @Override
    public List<CacheManager> getAllCacheManagers() {
        return unmodifiableManagers;
    }

    @Override
    public CacheManager getCacheManager(String cacheID) {
        for (CacheManager cacheManager : unmodifiableManagers) {
            if (cacheManager.getConfig().id().equals(cacheID)) {
                return cacheManager;
            }
        }
        throw new IllegalArgumentException("No cache with ID: " + cacheID);
    }
}
