/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import org.immutables.value.Value;

/**
 * Configuration of a {@link CheckedCache}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
public abstract class CheckedCacheConfig<K,V, E extends Exception> extends BaseCacheConfig {

    /**
     * Function used to obtain values of this Cache, given a key.
     */
    public abstract CheckedFunction<K,V, E> cacheFunction();

}
