/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.function.Function;

/**
 * Provider of (Factory for) Caches.
 *
 * @author Michael Vorburger
 */
public interface CacheProvider {

    <K,V, E extends Exception> Cache<K,V,E> createNonDistributedNonTransactionalCache(NewCache<K,V,E> newCache);


    abstract class NewCache<K,V,E extends Exception> {

        static <K,V,E extends Exception> NewCache<K,V,E> of(Class<K> keyClass, Class<V> valueClass) {
            return null; // TODO
        }

        abstract Class<?> callerClass(); // TODO find code in my DI project to look up BundleContext from callerClass

        abstract String name();

        abstract String description();

        abstract Function<K,V> cacheFunction();
    }

}
