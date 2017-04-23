/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.counters.api.OccurenceCounter;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Unit test for JobCoordinator.
 *
 * @author Michael Vorburger.ch
 */
public class JobCoordinatorTest {

    private static class WaitingCallable implements Callable<List<ListenableFuture<Void>>> {

        public volatile boolean isWaiting = false;
        Object lock = new Object();

        @Override
        public List<ListenableFuture<Void>> call() throws Exception {
            synchronized (lock) {
                isWaiting = true;
                lock.wait();
            }

            return null;
        }

        public void stopWaiting() {
            synchronized (lock) {
                isWaiting = false;
                lock.notify();
            }
        }
    }

    private static class TestCallable implements Callable<List<ListenableFuture<Void>>> {

        private final boolean isThrowingException;
        private final List<ListenableFuture<Void>> result;
        private final AtomicLong wasTried = new AtomicLong(0);

        TestCallable(boolean isThrowingException, int returnedListSize) {
            this.isThrowingException = isThrowingException;
            if (returnedListSize < 0) {
                this.result = null;
            } else {
                this.result = new ArrayList<>(returnedListSize);
                for (int i = 0; i < returnedListSize; i++) {
                    result.set(i, SettableFuture.create());
                }
            }
        }

        @Override
        public List<ListenableFuture<Void>> call() {
            wasTried.incrementAndGet();
            if (isThrowingException) {
                throw new RuntimeException("BOUM!");
            }
            return result;
        }

        SettableFuture<Void> getSettableFuture(int index) {
            return (SettableFuture<Void>) result.get(index);
        }

        long getRetries() {
            return wasTried.get();
        }
    }

    public @Rule LogRule logRule = new LogRule();
    // public static @ClassRule RunUntilFailureClassRule classRepeater = new
    // RunUntilFailureClassRule();
    // public @Rule RunUntilFailureRule repeater = new
    // RunUntilFailureRule(classRepeater);

    private JobCoordinatorImpl jobCoordinator;

    @Before
    public void setUp() {
        jobCoordinator = new JobCoordinatorImpl();
        OccurenceCounter.clearAllCounters(new String[] { ".*" }, new String[] { ".*" });
    }

    @After
    public void tearDown() {
        jobCoordinator.destroy();
        jobCoordinator = null;
    }

    @Test
    public void testNoExceptionReturnNull() {
        TestCallable testCallable = new TestCallable(false, -1);
        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getRetries()).isEqualTo(1);
    }

    @Test
    public void testNoExceptionReturnEmpty() {
        TestCallable testCallable = new TestCallable(false, 0);
        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getRetries()).isEqualTo(1);
    }

    @Test
    public void testAnException() {
        TestCallable testCallable = new TestCallable(true, -1);
        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getRetries()).isEqualTo(1);
    }

    @Test
    public void testOneJob() {
        WaitingCallable waitingCallable = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName().toString(), waitingCallable);
        assertCreated(1);
        Awaitility.await().until(() -> waitingCallable.isWaiting);
        assertIncomplete(1);
        waitingCallable.stopWaiting();
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertCleared(1);
    }

    @Test
    public void testTwoJobsSameKey() {
        WaitingCallable waitingCallable1 = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName().toString(), waitingCallable1);
        assertThat(jobCoordinator.getCreatedTaskCount()).isEqualTo(1);
        Awaitility.await().until(() -> waitingCallable1.isWaiting);
        WaitingCallable waitingCallable2 = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName().toString(), waitingCallable2);
        assertCreated(2);
        Awaitility.await().until(() -> waitingCallable1.isWaiting);

        // TODO: Make sure the job queue handler already attempted to run the second job (and didn't)

        assertIncomplete(2);
        assertPending(1);
        assertThat(!waitingCallable2.isWaiting);
        waitingCallable1.stopWaiting();
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(1L));
        Awaitility.await().until(() -> waitingCallable2.isWaiting);
        assertIncomplete(1);
        assertPending(0);
        waitingCallable2.stopWaiting();
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertCleared(2);
    }

    @Test
    public void testTwoJobsDifferentKeys() {
        WaitingCallable waitingCallable1 = new WaitingCallable();
        jobCoordinator.enqueueJob("key1", waitingCallable1);
        Awaitility.await().until(() -> waitingCallable1.isWaiting);
        WaitingCallable waitingCallable2 = new WaitingCallable();
        jobCoordinator.enqueueJob("key2", waitingCallable2);
        assertCreated(2);
        assertIncomplete(2);
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(() -> waitingCallable1.isWaiting);
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(() -> waitingCallable2.isWaiting);
        assertPending(0);
        waitingCallable1.stopWaiting();
        waitingCallable2.stopWaiting();
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertCleared(2);
    }

    private void assertCleared(int count) {
        assertThat(jobCoordinator.getClearedTaskCount()).isEqualTo(count);
    }

    private void assertCreated(int count) {
        assertThat(jobCoordinator.getCreatedTaskCount()).isEqualTo(count);
    }

    private void assertIncomplete(int count) {
        assertThat(jobCoordinator.getIncompleteTaskCount()).isEqualTo(count);
    }

    private void assertPending(int count) {
        assertThat(jobCoordinator.getPendingTaskCount()).isEqualTo(count);
    }

    // TODO expand this - significantly - until JobCoordinatorImpl has 100%
    // functional as well as line coverage:
    // * all permutations of above with all enqueueJob() variants -
    // rollbackWorker & retries
    // * keys! Jobs with the same key are run sequentially. Jobs with different
    // keys are run in parallel.
    // * SettableFuture set(), setException() & cancel(true) & cancel(false) -
    // for all permutations

}
