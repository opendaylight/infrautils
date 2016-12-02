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
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.ops.CachesMonitor;

/**
 * Implementation of CachesMonitor.
 *
 * @author Michael Vorburger.ch
 */
public class CachesMonitorImpl implements CachesMonitor {

    @Override
    public List<CacheManager> getAllCachesManagers() {
        return Collections.emptyList();
    }

}
