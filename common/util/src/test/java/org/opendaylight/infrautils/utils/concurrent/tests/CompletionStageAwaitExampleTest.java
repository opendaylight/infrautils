/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent.tests;

import static com.google.common.truth.Truth.assertThat;

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
import org.opendaylight.infrautils.utils.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test illustrating how to correctly await a {@link CompletionStage} in test code.
 *
 * @author Michael Vorburger.ch
 */
public class CompletionStageAwaitExampleTest {

    private static final Logger LOG = LoggerFactory.getLogger(CompletionStageAwaitExampleTest.class);

    public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(100);
    public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    private static final Executor SLOW_ASYNC_EXECUTOR = new SlowExecutor(
                Executors.newSingleThreadExecutor("SLOW_ASYNC_EXECUTOR", LOG));

    @Test
    public void testOneEventualValueCompletionStage() throws TimeoutException {
        // Imagine this is private code inside something you're testing:
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> 123, SLOW_ASYNC_EXECUTOR);

        // Imagine you can't see the CompletableFuture, what you're testing just returns CompletionStage:
        CompletionStage<Integer> completionStage = completableFuture;

        // so you cannot do this (because join(), and get(), are on CompletableFuture, not CompletionStage)
        // assertThat(completionStage.join()).isEqualTo(123);

        // and should not do this (because toCompletableFuture may throw an UnsupportedOperationException)
        // assertThat(completionStage.toCompletableFuture().join()).isEqualTo(123);

        // This is the correct way to await and assert the expected result:
        assertThat(CompletionStageTestAwaiter.await500ms(completionStage)).isEqualTo(123);
    }

    // TODO testOneEventualExceptionCompletionStage()

    // TODO testTwoValueCompletionStages()

    // TODO testTwoExceptionCompletionStages()

    // TODO testOneImmediateValueCompletionStage() {

    // TODO testOneImmediateExceptionCompletionStage()

}
