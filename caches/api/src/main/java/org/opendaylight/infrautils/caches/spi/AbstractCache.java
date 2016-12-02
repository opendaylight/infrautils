/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.spi;

import java.util.Objects;
import javax.annotation.Nullable;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.exceptions.BadCacheFunctionRuntimeException;

/**
 * Base Cache, to be extended by implementors.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCache<K,V> implements Cache<K,V> {

    @Override
    public final V get(K key) throws BadCacheFunctionRuntimeException {
        Objects.requireNonNull(key, "null key (not supported)");
        V value = getNullable(key);
        if (value == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value for key: " + key);
        }
        return value;
    }

    protected abstract @Nullable V getNullable(K key);

}
