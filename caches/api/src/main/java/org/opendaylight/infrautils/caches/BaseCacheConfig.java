/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.opendaylight.infrautils.utils.types.IDs;

/**
 * Base class for {@link CacheConfig} and {@link CheckedCacheConfig}.
 *
 * @author Michael Vorburger.ch
 */
public abstract class BaseCacheConfig {

    /**
     * Instance of the class "containing" this Cache.
     * Used by {@link CacheManagers} for display to end-user.
     * Also used under OSGi to close() the cache on bundle unload.
     */
    public abstract Object anchor(); // TODO find code in my DI project to look up BundleContext from callerClass

    /**
     * Short ID of this Cache.
     * Optional; default is anchor' class name.
     * Recommended if a class has more than one Cache instance.
     * Used only by {@link CacheManagers} for display to end-user.
     * Any code requiring to identify a instances would just hold on to the {@link Cache} instance.
     * Must be all lower case letters only, may use dot as separator, but no spaces or other characters.
     */
    @Default public String id() {
        return anchor().getClass().getName();
    }

    /**
     * Human readable 1 line description of this Cache.
     * Used by {@link CacheManagers} for display to end-user.
     * Optional; default is no description.
     */
    @Default public String description() {
        return "";
    }

    /**
     * Validates this cache configuration.
     */
    @Value.Check
    protected void check() {
        IDs.checkOnlyAZ09Dot(id());
    }

}
