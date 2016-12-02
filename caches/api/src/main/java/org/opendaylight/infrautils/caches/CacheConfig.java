/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import static org.immutables.value.Value.Style.ImplementationVisibility.PRIVATE;

import java.util.function.Function;
import org.immutables.value.Value;
import org.immutables.value.Value.Style;

/**
 * Configuration of a {@link Cache}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Style(visibility = PRIVATE) // TODO staged | strict
public abstract class CacheConfig<K,V> extends BaseCacheConfig {

    /**
     * Function used to obtain values of this Cache, given a key.
     */
    public abstract Function<K,V> cacheFunction();

}
