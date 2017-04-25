/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.guavafork.internal;

import static com.google.common.truth.Truth.assertThat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Test;

public class LearnCompletableFutureTest {

    @Test
    public void testCompletableFutureCompletion() throws Exception {
        assertThat(CompletableFuture.supplyAsync(() -> {
            return null;
        }).join()).isNull();
    }

    @Test
    public void testNestedCompletedFuture() throws Exception {
        CompletableFuture<CompletableFuture<Object>> f = CompletableFuture.supplyAsync(() -> {
            return CompletableFuture.completedFuture(null);
        });
        assertThat(f.join()).isNull();
    }

    @Test(expected = CompletionException.class)
    public void testCompletableFutureException() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("boum");
        }).join();
    }

    @Test(expected = CompletionException.class)
    public void testNestedCompletableFutureException() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            return new CompletableFuture<>().completeExceptionally(new RuntimeException("boum"));
        }).join();
    }

}
