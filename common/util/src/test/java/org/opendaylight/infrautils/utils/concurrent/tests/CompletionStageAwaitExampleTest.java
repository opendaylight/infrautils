/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent.tests;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.RunUntilFailureClassRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureRule;
import org.opendaylight.infrautils.testutils.concurrent.CompletionStageTestAwaiter;
import org.opendaylight.infrautils.testutils.concurrent.SlowExecutor;
import org.opendaylight.infrautils.utils.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test illustrating how to correctly await a {@link CompletionStage} in test code,
 * and unit Test for {@link CompletionStageTestAwaiter}.
 *
 * @author Michael Vorburger.ch
 * @deprecated This class demonstrates deprecated functionality and is slated for removal.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
public class CompletionStageAwaitExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(CompletionStageAwaitExampleTest.class);

    @ClassRule public static RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(100);
    @Rule public RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    private static final Executor SLOW_ASYNC_EXECUTOR = new SlowExecutor(
                Executors.newListeningSingleThreadExecutor("SLOW_ASYNC_EXECUTOR", LOG));

    @Test
    public void testOneEventualValueCompletionStage() throws TimeoutException {
        // Imagine this is private code inside something you're testing:
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 123, SLOW_ASYNC_EXECUTOR);

        // Imagine you can't see the CompletableFuture, what you're testing just returns CompletionStage:
        CompletionStage<Integer> completionStage = completableFuture;

        // so you cannot do this (because join(), and get(), are on CompletableFuture, not CompletionStage)
        // assertEquals(123, completionStage.join());

        // and should not do this (because toCompletableFuture may throw an UnsupportedOperationException)
        // assertEquals(123, completionStage.toCompletableFuture().join());

        // This is the correct way to await and assert the expected result:
        assertEquals(123, CompletionStageTestAwaiter.await500ms(completionStage).intValue());
    }

    // TODO testOneEventualExceptionalCompletionStage()

    @Test
    public void testOneImmediateValueCompletableFuture() throws TimeoutException {
        assertEquals("hello", CompletionStageTestAwaiter.await500ms(CompletableFuture.completedFuture("hello")));
    }
}
