/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.ops;

import org.immutables.value.Value;

/**
 * Statistics about a Cache.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
public interface CacheStats {

    long estimatedCurrentEntries();

}
