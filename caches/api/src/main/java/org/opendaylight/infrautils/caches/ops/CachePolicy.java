/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.ops;

import static org.immutables.value.Value.Style.ImplementationVisibility.PRIVATE;

import java.util.Map;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CheckedCache;

/**
 * Policy of a {@link Cache} (or {@link CheckedCache}).
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = PRIVATE, strictBuilder = false)
public interface CachePolicy {

    @Default default boolean statsEnabled() {
        return false;
    }

    @Default default long maxEntries() {
        return 743; // arbitrary
    }

    Map<String,Long> extensions();
    //     TODO how to set?  Builder supported how? or split:
    //         List<String> extensions();
    //         String extension(String name);
}
