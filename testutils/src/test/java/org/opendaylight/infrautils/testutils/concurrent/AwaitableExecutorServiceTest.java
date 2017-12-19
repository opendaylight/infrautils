/*
 * Copyright © 2017 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.singletonList;

import com.google.common.util.concurrent.Futures;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.junit.Test;

public class AwaitableExecutorServiceTest {

    @Test
    public void testBasicWait() throws InterruptedException, ExecutionException {
        long millis = 500;
        testAndVerifyTimeBounds(executorService ->
            executorService.submit(() -> {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    // Ignored
                }
            }), millis, 1000).get();
    }

    @Test
    public void testIncompleteWait() throws InterruptedException, ExecutionException {
        long millis = 1500;
        testAndVerifyTimeBounds(executorService ->
                executorService.submit(() -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }), millis, 1000).get();
    }

    @Test
    public void testExecute() {
        long millis = 500;
        testAndVerifyTimeBounds(executorService -> {
            executorService.execute(() -> {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    // Ignored
                }
            });
            return null;
        }, millis, 1000);
    }

    @Test
    public void testSubmitCallable() {
        long millis = 500;
        testAndVerifyTimeBounds(executorService ->
                executorService.submit(() -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                    return true;
                }), millis, 1000);
    }

    @Test
    public void testSubmitRunnableWithResult() {
        long millis = 500;
        testAndVerifyTimeBounds(executorService ->
                executorService.submit(() -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }, true), millis, 1000);
    }

    @Test
    public void testSubmitRunnable() {
        long millis = 500;
        testAndVerifyTimeBounds(executorService ->
                executorService.submit(() -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }), millis, 1000);
    }

    @Test
    public void testInvokeAllWithoutTimeout() throws InterruptedException, ExecutionException {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        for (Future<Boolean> f : testAndVerifyTimeBounds(executorService -> {
            try {
                return executorService.invokeAll(Arrays.asList(task, task));
            } catch (InterruptedException e) {
                return singletonList(Futures.<Boolean>immediateCancelledFuture());
            }
        }, millis, 1000)) {
            assertThat(f.get()).isTrue();
        }
    }

    @Test
    public void testInvokeAllWithTimeout() throws InterruptedException, ExecutionException {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        long timeout = 1000;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        for (Future<Boolean> f : testAndVerifyTimeBounds(executorService -> {
            try {
                return executorService.invokeAll(Arrays.asList(task, task), timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return singletonList(Futures.<Boolean>immediateCancelledFuture());
            }
        }, millis, timeout)) {
            assertThat(f.get()).isTrue();
        }
    }

    @Test
    @SuppressWarnings("cast")
    public void testInvokeAnyWithoutTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        assertThat((Boolean) testAndVerifyTimeBounds(executorService -> {
            try {
                return executorService.invokeAny(Arrays.asList(task, task));
            } catch (InterruptedException | ExecutionException e) {
                return FALSE;
            }
        }, millis, 1000)).isTrue();
    }

    @Test
    @SuppressWarnings("cast")
    public void testInvokeAnyWithTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        long timeout = 1000;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        assertThat((Boolean) testAndVerifyTimeBounds(executorService -> {
            try {
                return executorService.invokeAny(Arrays.asList(task, task), timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return FALSE;
            }
        }, millis, timeout)).isTrue();
    }

    private static <T> T testAndVerifyTimeBounds(Function<ExecutorService, T> test, long executionMS, long timeoutMS) {
        AwaitableExecutorService executorService = new AwaitableExecutorService(Executors.newFixedThreadPool(4));
        long start = System.currentTimeMillis();
        T future = test.apply(executorService);
        try {
            assertThat(executorService.awaitCompletion(timeoutMS, TimeUnit.MILLISECONDS)).isEqualTo(
                    executionMS < timeoutMS);
        } catch (InterruptedException e) {
            // Ignored
        }
        long elapsed = System.currentTimeMillis() - start;
        if (executionMS < timeoutMS) {
            assertThat(elapsed).isAtLeast(executionMS);
        } else {
            assertThat(elapsed).isAtLeast(timeoutMS);
        }
        return future;
    }
}
