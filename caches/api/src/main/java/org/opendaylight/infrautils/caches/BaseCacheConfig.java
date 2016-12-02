/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Locale;
import java.util.regex.Pattern;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Base class for {@link CacheConfig} and {@link CheckedCacheConfig}.
 *
 * @author Michael Vorburger.ch
 */
public abstract class BaseCacheConfig {

    private static final Pattern ID_REGEXP = Pattern.compile("[a-z0-9\\.]+");

    // TODO key & value classes? What for? If yes, as static build method?
//    static <K,V,E extends Exception> CacheConfigBuilder<K,V,E> builder(Class<K> keyClass, Class<V> valueClass) {
//        return ImmutableCacheConfig.builder();
//    }

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
    public @Default String id() {
        return anchor().getClass().getName().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Human readable 1 line description of this Cache.
     * Used by {@link CacheManagers} for display to end-user.
     * Optional; default is no description.
     */
    public @Default String description() {
        return "";
    }

    @Value.Check
    protected void check() {
        if (!ID_REGEXP.matcher(id()).matches()) {
            throw new IllegalArgumentException("Invalid ID: " + id());
        }
    }

}
