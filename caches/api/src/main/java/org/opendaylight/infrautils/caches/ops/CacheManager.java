/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.ops;

import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.Cache;

/**
 * Manage a Cache.
 *
 * <p>Allows to change policy settings at run-time.
 *
 * <p>Intentionally does <b>NOT</b> give direct programmatic access to the {@link Cache} API.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface CacheManager {

    BaseCacheConfig getConfig();

    CacheStats getStats();

    CachePolicy getPolicy();

    void setPolicy(CachePolicy newPolicy);

    void evictAll();

}
