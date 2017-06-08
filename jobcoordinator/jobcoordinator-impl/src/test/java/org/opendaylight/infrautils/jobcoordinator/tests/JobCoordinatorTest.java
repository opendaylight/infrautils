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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.counters.api.OccurenceCounter;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureClassRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureRule;

/**
 * Unit test for JobCoordinator.
 *
 * @author Michael Vorburger.ch
 */
public class JobCoordinatorTest {

    private static final RuntimeException JOB_EXCEPTION = new JobException("Job is failed intentionally");

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

    private static class JobException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private JobException(String message) {
            super(message);
        }
    }

    private static class TestCallable implements Callable<List<ListenableFuture<Void>>> {

        private final boolean isThrowingException;
        private final int returnedListSize;
        private final List<ListenableFuture<Void>> result;
        private final AtomicLong wasTried = new AtomicLong(0);

        TestCallable(boolean isThrowingException, int returnedListSize) {
            this.isThrowingException = isThrowingException;
            this.returnedListSize = returnedListSize;
            if (returnedListSize < 0) {
                this.result = null;
            } else {
                this.result = new ArrayList<>(returnedListSize);
                ListenableFuture<Void> future = isThrowingException
                        ? Futures.immediateFailedFuture(JOB_EXCEPTION) : SettableFuture.create();
                for (int i = 0; i < returnedListSize; i++) {
                    result.add(i, future);
                }
            }
        }

        @Override
        public List<ListenableFuture<Void>> call() {
            wasTried.incrementAndGet();
            if (isThrowingException && returnedListSize < 0) {
                throw JOB_EXCEPTION;
            }
            return result;
        }

        long getRetries() {
            return wasTried.get();
        }
    }

    private static class RollbackTask extends RollbackCallable {
        private final AtomicLong wasTried = new AtomicLong(0);

        @Override
        public List<ListenableFuture<Void>> call() throws Exception {
            wasTried.incrementAndGet();
            return Collections.emptyList();
        }

        long getRetries() {
            return wasTried.get();
        }
    }

    public @Rule LogRule logRule = new LogRule();
    public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(25);
    public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    private JobCoordinatorImpl jobCoordinator;

    @Before
    public void setUp() {
        jobCoordinator = new JobCoordinatorImpl();
        OccurenceCounter.clearAllCounters(new String[] { ".*" }, new String[] { ".*" });
        Awaitility.setDefaultTimeout(15, TimeUnit.SECONDS);
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
        assertFailed(1);
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

        assertExecuteAttempts(1);

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

    @Test
    public void testJobRetriesWhenException() {
        TestCallable testCallable = new TestCallable(true, 1);
        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getRetries()).isEqualTo(3);
        assertRetry(3);
        assertFailed(1);
    }

    @Test
    public void testTwoJobRetriesWhenException() {
        TestCallable testCallable1 = new TestCallable(true, 1);
        jobCoordinator.enqueueJob("key1", testCallable1, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable1.getRetries()).isEqualTo(3);

        TestCallable testCallable2 = new TestCallable(true, 1);
        jobCoordinator.enqueueJob("key2", testCallable2, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable2.getRetries()).isEqualTo(3);

        assertRetry(6);
        assertFailed(2);
    }

    @Test
    public void testJobRetriesWithRollback() {
        TestCallable testCallable = new TestCallable(true, 1);
        RollbackTask rollbackCallable = new RollbackTask();

        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable, rollbackCallable, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getRetries()).isEqualTo(3);
        assertThat(rollbackCallable.getRetries()).isEqualTo(1);
        assertRetry(3);
        assertFailed(1);
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

    private void assertFailed(int count) {
        assertThat(jobCoordinator.getFailedJobCount()).isEqualTo(count);
    }

    private void assertRetry(int count) {
        assertThat(jobCoordinator.getRetriesCount()).isEqualTo(count);
    }

    private void assertExecuteAttempts(int count) {
        assertThat(jobCoordinator.getExecuteAttempts()).isEqualTo(count);
    }

}
