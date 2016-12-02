/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * {@link CheckedCache}'s Function, can throw checked Exception.
 *
 * <p>See also {@link CacheFunction}.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedCacheFunction<K, V, E extends Exception> {

    V get(K key) throws E;

}
