/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.function;

import java.util.concurrent.Callable;

/**
 * {@link Callable} which throws a specific generically parameterized type of
 * checked exception instead of a fixed {@link Exception}.
 *
 * @author Michael Vorburger.ch
 */
public interface CheckedCallable<V, E extends Exception> extends Callable<V> {

    @Override
    V call() throws E;

}
