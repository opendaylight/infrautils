/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.util.concurrent.AbstractFuture;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

// package local, not public
class CompletableToListenableFutureWrapper<V> extends AbstractFuture<V> implements BiConsumer<V, Throwable> {

    // This implementation is "strongly inspired" ;) by spotify/futures-extra's class of the same name

    CompletableToListenableFutureWrapper(CompletableFuture<V> completableFuture) {
        completableFuture.whenComplete(this);
    }

    @Override
    public void accept(V value, Throwable throwable) {
        if (throwable != null) {
            setException(throwable);
        } else {
            set(value);
        }
    }
}
