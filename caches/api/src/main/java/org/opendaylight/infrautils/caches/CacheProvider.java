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
 * Provider (AKA factory) of Caches.
 *
 * <p>Users typically obtain an implementation of this from the OSGi service registry.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface CacheProvider {

    <K,V, E extends Exception> Cache<K,V,E> createNonDistributedNonTransactionalCache(CacheConfig<K,V,E> cacheConfig);

}
