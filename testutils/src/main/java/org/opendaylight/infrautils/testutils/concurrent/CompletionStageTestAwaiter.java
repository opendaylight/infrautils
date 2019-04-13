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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Test utility to await the completion of a {@link CompletionStage}.
 *
 * <p>Note that if you have a {@link CompletableFuture} instead of a {@link CompletionStage},
 * then you could just use {@link CompletableFuture#join()}, which does the same thing as
 * the {@link #await500ms(CompletionStage)} here.  However, to test operations
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

    /**
     * Await max. 500ms for the CompletionStage argument to complete.
     *
     * @throws CompletionException if it completed unsuccessfully
     * @throws CancellationException if it got cancelled while awaiting completion
     * @throws TimeoutException if it timed out awaiting completion
     */
    // Suppress because (a) CS doesn't get initCause() and (b) CompletionException with getCause() is what we want
    @SuppressWarnings("checkstyle:AvoidHidingCauseException")
    public static <T> T await500ms(CompletionStage<T> completionStage) throws TimeoutException {
        return await(completionStage, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Await a configurable amount of time for the CompletionStage argument to complete.
     *
     * @throws CompletionException if it completed unsuccessfully
     * @throws CancellationException if it got cancelled while awaiting completion
     * @throws TimeoutException if it timed out awaiting completion
     */
    // Suppress because (a) CS doesn't get initCause() and (b) CompletionException with getCause() is what we want
    @SuppressWarnings("checkstyle:AvoidHidingCauseException")
    public static <T> T await(CompletionStage<T> completionStage, long timeout, TimeUnit unit) throws TimeoutException {
        try {
            // we first try our luck and see if toCompletableFuture() is available:
            return completionStage.toCompletableFuture().get(timeout, unit);
        } catch (UnsupportedOperationException e) {
            // do NOT log, we're kinda half-expecting this, and can handle it (that's the whole point of this utility!)
            return new CompletionStageTestAwaiter<>(completionStage).wait(timeout, unit);
        // the following exceptions from get() need to be translated to fit the exceptions that
        // join()'s method signature would have thrown (but join does not have a timeout variant)
        } catch (InterruptedException e) {
            CancellationException cancellationException = new CancellationException(e.getMessage());
            cancellationException.initCause(e);
            throw cancellationException;
        } catch (ExecutionException e) {
            throw new CompletionException(e.getMessage(), e.getCause());
        }
    }

    private final AtomicReference<T> eventualValue = new AtomicReference<>();
    private final AtomicReference<Throwable> eventualThrowable  = new AtomicReference<>();

    private CompletionStageTestAwaiter(CompletionStage<T> completionStage) {
        completionStage.whenComplete((value, throwable) -> {
            eventualValue.set(value);
            eventualThrowable.set(throwable);
        });
    }

    @SuppressWarnings("NullAway")
    private T wait(long timeout, TimeUnit unit) {
        Awaitility.await(getClass().getName())
            .pollDelay(0, MILLISECONDS)
            .pollInterval(100, MILLISECONDS)
            .atMost(timeout, unit)
            .until(() -> eventualValue.get() != null || eventualThrowable.get() != null);

        Throwable throwable = eventualThrowable.get();
        if (throwable != null) {
            // This is like what CompletableFuture#join() does:
            Throwables.throwIfUnchecked(throwable);
            throw new CompletionException(throwable);
        }

        // Should use org.opendaylight.infrautils.utils.lastnpe.NonNulls.castToNonNull(T) here
        // instead of @SuppressWarnings("NullAway") above, but cannot just because testutils
        // cannot depend on utils (which depends on testutils).
        return (@NonNull T) eventualValue.get();
    }
}
