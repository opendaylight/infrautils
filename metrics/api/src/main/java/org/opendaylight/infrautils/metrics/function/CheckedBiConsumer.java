/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.function;

/**
 * Represents an operation that accepts two input arguments and returns no
 * result, but can throw a checked exception.  This is the two-arity specialization of
 * {@link CheckedConsumer}.
 * Unlike most other functional interfaces, {@code BiConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation.
 * @param <U> the type of the second argument to the operation.
 * @param <E> the type of checked exception the operation might throw.
 * @see CheckedConsumer
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, U, E extends Exception> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param input1 the first input argument.
     * @param input2 the second input argument.
     *
     * @throws E if an error occurs.
     */
    void accept(T input1, U input2) throws E;
}
