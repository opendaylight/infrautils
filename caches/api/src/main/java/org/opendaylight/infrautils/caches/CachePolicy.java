/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import static org.immutables.value.Value.Style.ImplementationVisibility.PRIVATE;

import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Policy of a {@link Cache} (or {@link CheckedCache}).
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = PRIVATE, strictBuilder = false)
public interface CachePolicy {
    long UNLIMITED_ENTRIES = -1;

    /**
     * Whether this cache has it statics enabled.
     * This may cause it to have a bit more runtime overhead than it would have if it does not.
     * {@link CacheStats} are obtained via {@link CacheManager#getStats()}
     */
    @Default default boolean statsEnabled() {
        return false;
    }

    /**
     * Specifies the maximum number of entries the cache may contain. When this limit is reached, existing entries
     * are evicted to accomodate new entries.
     *
     * <p>
     * The default is {@link #UNLIMITED_ENTRIES}.
     *
     * <p>
     * Users are encourage to set the maximum entries policy suitable to their actual usage of the cache; either
     * programmatically in code, or by runtime configuration.
     */
    @Default default long maxEntries() {
        return UNLIMITED_ENTRIES;
    }
}
