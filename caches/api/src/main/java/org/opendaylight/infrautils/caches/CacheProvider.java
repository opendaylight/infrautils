/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * Provider (AKA factory) of {@link Cache}s.
 *
 * <p>Users typically obtain an implementation of this from the OSGi service registry.
 *
 * <p>{@link Cache} instances produced by this service are neither <i>transactional</i>,
 * nor <i>distributed</i>, nor <i>persistent</i>.
 *
 * <p>Implementations of this interface are expected to be thread-safe.
 *
 * @author Michael Vorburger.ch
 */
public interface CacheProvider {

    // In the future there could be other such interfaces
    // imagine a ClusteredCacheProvider or a TxCacheProvider, which could return
    // Cache instances which are that, if there was a need for it - but currently there is not-

    /**
     * Creates a brand new {@link Cache} (API with unchecked exceptions), based on the passed configuration and policy.
     * It is the caller's responsibility to {@link Cache#close()} a Cache obtained from this when they stop.
     */
    <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig, CachePolicy initialPolicy);

    /**
     * Creates a brand new {@link Cache} (API with unchecked exceptions), based on
     * the passed configuration and a default policy.
     * It is the caller's responsibility to {@link Cache#close()} a Cache obtained from this when they stop.
     */
    <K, V> Cache<K, V> newCache(CacheConfig<K, V> cacheConfig);

    /**
     * Creates a brand new {@link CheckedCache} (API for checked exceptions), based
     * on the passed configuration and policy.
     * It is the caller's responsibility to {@link Cache#close()} a Cache obtained from this when they stop.
     */
    <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(
            CheckedCacheConfig<K, V, E> cacheConfig, CachePolicy initialPolicy);

    /**
     * Creates a brand new {@link CheckedCache} (API for checked exceptions), based
     * on the passed configuration and a default policy.
     * It is the caller's responsibility to {@link Cache#close()} a Cache obtained from this when they stop.
     */
    <K, V, E extends Exception> CheckedCache<K, V, E> newCheckedCache(CheckedCacheConfig<K, V, E> cacheConfig);

}
