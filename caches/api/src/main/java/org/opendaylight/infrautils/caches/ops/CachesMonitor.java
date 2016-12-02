/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.ops;

import java.util.List;

/**
 * Service to monitor caches.
 *
 * <p>Used by e.g. CLI commands, web UIs, etc.
 *
 * @author Michael Vorburger.ch
 */
public interface CachesMonitor {

    List<CacheStats> getAllCachesDetails();

}
