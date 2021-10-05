/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.function;

/**
 * Functional interface similar to {@link Runnable} (but not technically
 * extending Runnable, because it cannot; and doesn't have to) which can throw a
 * generically parameterized type of checked exception.
 *
 * @param <E> the type of the Exception to the operation
 *
 * @author Michael Vorburger.ch
 * @deprecated This class is going to be moved to {@code metrics.function}
 */
@Deprecated(since = "2.0.7", forRemoval = true)
@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

    void run() throws E;

}
