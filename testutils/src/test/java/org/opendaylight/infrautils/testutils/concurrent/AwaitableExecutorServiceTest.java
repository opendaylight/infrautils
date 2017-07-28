package org.opendaylight.infrautils.testutils.concurrent;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class AwaitableExecutorServiceTest {
    @Test
    public void testBasicWait() {
        AwaitableExecutorService executorService = new AwaitableExecutorService(Executors.newFixedThreadPool(4));
        long start = System.currentTimeMillis();
        long millis = 500;
        long timeout = 1000;
        executorService.submit(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // Ignored
            }
        });
        try {
            assertThat(executorService.awaitCompletion(timeout, TimeUnit.MILLISECONDS)).isTrue();
        } catch (InterruptedException e) {
            // Ignored
        }
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isGreaterThan(millis);
    }

    @Test
    public void testIncompleteWait() {
        AwaitableExecutorService executorService = new AwaitableExecutorService(Executors.newFixedThreadPool(4));
        long start = System.currentTimeMillis();
        long millis = 1500;
        long timeout = 1000;
        executorService.submit(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // Ignored
            }
        });
        try {
            assertThat(executorService.awaitCompletion(1000, TimeUnit.MILLISECONDS)).isFalse();
        } catch (InterruptedException e) {
            // Ignored
        }
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isGreaterThan(timeout);
    }
}
