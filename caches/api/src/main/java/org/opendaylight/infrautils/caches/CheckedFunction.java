/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.function.Function;

/**
 * {@link Function} which can throw a checked Exception.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedFunction<K, V, E extends Exception> {

    V apply(K key) throws E;

}
