/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.baseimpl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.infrautils.caches.CheckedCache;

/**
 * Cache with null handling, useful for API implementors (not users).
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
public final class DelegatingNullSafeCheckedCache<K, V, E extends Exception> extends DelegatingNullSafeBaseCache<K, V>
        implements CheckedCache<K, V, E> {
    private final CheckedCache<K, V, E> delegate;

    public DelegatingNullSafeCheckedCache(CheckedCache<K, V, E> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected CheckedCache<K, V, E> delegate() {
        return delegate;
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public V get(K key) throws E {
        return checkReturnValue(key, delegate.get(requireNonNull(key, "null key (not supported)")));
    }

    @Override
    public ImmutableMap<K, V> get(Iterable<? extends K> keys) throws E {
        return checkReturnValue(delegate.get(checkNonNullKeys(keys)));
    }
}
