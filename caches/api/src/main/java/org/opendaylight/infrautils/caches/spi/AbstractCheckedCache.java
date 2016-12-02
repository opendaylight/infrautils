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
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.CheckedCache;

/**
 * Base Cache, to be extended by implementors.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCheckedCache<K,V,E extends Exception> implements CheckedCache<K,V,E> {

    @Override
    public final V get(K key) throws BadCacheFunctionRuntimeException, E {
        Objects.requireNonNull(key, "null key (not supported)");
        V value = getNullable(key);
        if (value == null) {
            throw new BadCacheFunctionRuntimeException("Cache's function returned null value for key: " + key);
        }
        return value;
    }

    protected abstract @Nullable V getNullable(K key) throws E;

}
