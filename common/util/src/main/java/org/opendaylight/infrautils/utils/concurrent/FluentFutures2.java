/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Utilities for {@link FluentFuture}.
 * There are more such utilities in yangtools' FluentFutures.
 *
 * @author Michael Vorburger.ch
 */
public final class FluentFutures2 {

    private FluentFutures2() {
    }

    /**
     * Converts Guava's (new) FluentFuture to its (old) CheckedFuture.
     * This is useful while migrating APIs.
     *
     * <p>The implementation just uses {@link Futures#makeChecked(ListenableFuture, com.google.common.base.Function)},
     * so this is pure syntactic sugar - making it easier to remember and find: you have a FluentFuture and you find
     * this with convenience conversion methods - like we have for other types in this package.
     *
     * @deprecated Replace {@link CheckedFuture} usages in your code by
     *             {@link FluentFuture} (or {@link CompletionStage})
     */
    @Deprecated // TODO remove this method when all of its usages have been removed and its unnecessary
    public <V, E extends Exception> CheckedFuture<V, E> toChecked(FluentFuture<V> future,
            Function<? super Exception, E> mapper) {
        return Futures.makeChecked(future , convert(mapper));
    }

    private static <I, O> com.google.common.base.Function<I, O> convert(Function<I, O> julFunction) {
        return arg -> julFunction.apply(arg);
    }

}
