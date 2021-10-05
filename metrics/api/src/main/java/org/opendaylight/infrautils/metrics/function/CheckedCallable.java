/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.function;

import java.util.concurrent.Callable;

/**
 * {@link Callable} which throws a specific generically parameterized type of
 * checked exception instead of a fixed {@link Exception}.  (Not technically
 * extending Callable, because of a javac generics bug; that's OK, it doesn't have to.)
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedCallable<V, E extends Exception> /* extends Callable<V> */ {

    // Huh - interesting, extends Callable<V>
    //   OK : Eclipse JDT ECJ Photon Milestone 2 (4.8.0M2) Build id: 20170922-0530
    //   NOK: OpenJDK javac 1.8.0_151-b12

    // @Override
    V call() throws E;

}
