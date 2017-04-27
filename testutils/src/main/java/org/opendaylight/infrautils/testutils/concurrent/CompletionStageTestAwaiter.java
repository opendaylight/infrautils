/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.base.Throwables;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;

/**
 * Test utility to await the completion of a {@link CompletionStage}.
 *
 * <p>Note that if you have a {@link CompletableFuture} instead of a {@link CompletionStage},
 * then you could just use {@link CompletableFuture#join()}, which does the same thing as
 * the {@link #await(CompletionStage)} here.  However, to test operations
 * which return only a <code>CompletionStage</code> but not a full <code>CompletableFuture</code>,
 * you use this.  (<code>CompletionStage</code> is often a more suitable operation return type,
 * because it <i>"does not define methods for initially creating,
 * forcibly completing normally or exceptionally, probing completion
 * status or results, or awaiting completion of a stage"</i>.)
 *
 * <p>Note that the {@link CompletionStage#toCompletableFuture()} "back-door" may <i>"throw an
 * UnsupportedOperationException if an implementation does not inter-operate
 * with CompletableFuture"</i>.
 *
 * <p>In production code, you normally <b>never should</b> have to await/join/get results
 * returned as <code>CompletionStage</code>.  If you are, you're probably using an API wrong.
 * (Valid deviations to this rule include scenarios where you bridge an async API to an existing
 * legacy API, which you cannot change, using synchronous signature methods which do not return an
 * async types such as <code>CompletionStage</code> or (a subtype of) {@link Future}.)
 *
 * <p>See the <code>CompletionStageAwaitExampleTest</code> for an illustration how to use this.
 *
 * @author Michael Vorburger.ch
 */
public final class CompletionStageTestAwaiter<T> {

    // TODO Variant to await eventually .. at end of test, while doing other things in the mean time; and @Rule for that

    // TODO Google Truth integration, to be able to do assertThat(CompletionStage)
    //   .[eventually]CompletedWithValue(value) & .[eventually]CompletedWithFailure(Throwable)

    public static <T> T await(CompletionStage<T> completionStage) throws CompletionException, CancellationException {
        return new CompletionStageTestAwaiter<>(completionStage).await();
    }

    private final AtomicReference<T> eventualValue = new AtomicReference<>();
    private final AtomicReference<Throwable> eventualThrowable  = new AtomicReference<>();

    private CompletionStageTestAwaiter(CompletionStage<T> completionStage) {
        completionStage.whenComplete((value, throwable) -> {
            eventualValue.set(value);
            eventualThrowable.set(throwable);
        });
    }

    private T await() throws CompletionException, CancellationException {
        return await(500, TimeUnit.MILLISECONDS);
    }

    private T await(long timeout, TimeUnit unit) throws CompletionException, CancellationException {
        Awaitility.await(getClass().getName())
            .pollDelay(0, MILLISECONDS)
            .pollInterval(100, MILLISECONDS)
            .atMost(timeout, unit)
            .until(() -> eventualValue.get() != null || eventualThrowable.get() != null);

        Throwable throwable = eventualThrowable.get();
        if (throwable != null) {
            // This is like what CompletableFuture#join() does:
            Throwables.propagateIfPossible(throwable);
            throw new CompletionException(throwable);
        } else {
            return eventualValue.get();
        }
    }

}
