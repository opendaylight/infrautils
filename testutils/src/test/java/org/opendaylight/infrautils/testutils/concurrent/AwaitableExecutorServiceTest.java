package org.opendaylight.infrautils.testutils.concurrent;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.junit.Test;

public class AwaitableExecutorServiceTest {
    @Test
    public void testBasicWait() {
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
    public void testIncompleteWait() {
        long millis = 1500;
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
    public void testExecute() {
        long millis = 500;
        testAndVerifyTimeBounds(executorService ->
                executorService.execute(() -> {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        // Ignored
                    }
                }), millis, 1000);
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
    public void testInvokeAllWithoutTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        testAndVerifyTimeBounds(executorService -> {
            try {
                executorService.invokeAll(Arrays.asList(task, task));
            } catch (InterruptedException e) {
                // Ignored
            }
        }, millis, 1000);
    }

    @Test
    public void testInvokeAllWithTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        long timeout = 1000;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        testAndVerifyTimeBounds(executorService -> {
            try {
                executorService.invokeAll(Arrays.asList(task, task), timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignored
            }
        }, millis, timeout);
    }

    @Test
    public void testInvokeAnyWithoutTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        testAndVerifyTimeBounds(executorService -> {
            try {
                executorService.invokeAny(Arrays.asList(task, task));
            } catch (InterruptedException | ExecutionException e) {
                // Ignored
            }
        }, millis, 1000);
    }

    @Test
    public void testInvokeAnyWithTimeout() {
        // Ensure we complete even with a single thread executing jobs
        long millis = 250;
        long timeout = 1000;
        Callable<Boolean> task = () -> {
            Thread.sleep(millis);
            return true;
        };
        testAndVerifyTimeBounds(executorService -> {
            try {
                executorService.invokeAny(Arrays.asList(task, task), timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // Ignored
            }
        }, millis, timeout);
    }

    private void testAndVerifyTimeBounds(Consumer<ExecutorService> test, long executionMillis, long timeoutMillis) {
        AwaitableExecutorService executorService = new AwaitableExecutorService(Executors.newFixedThreadPool(4));
        long start = System.currentTimeMillis();
        test.accept(executorService);
        try {
            assertThat(executorService.awaitCompletion(timeoutMillis, TimeUnit.MILLISECONDS)).isEqualTo(
                    executionMillis < timeoutMillis);
        } catch (InterruptedException e) {
            // Ignored
        }
        long elapsed = System.currentTimeMillis() - start;
        if (executionMillis < timeoutMillis) {
            assertThat(elapsed).isAtLeast(executionMillis);
        } else {
            assertThat(elapsed).isAtLeast(timeoutMillis);
        }
    }
}
