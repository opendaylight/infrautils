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
import static org.junit.Assert.assertFalse;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureClassRule;
import org.opendaylight.infrautils.testutils.RunUntilFailureRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for JobCoordinator.
 *
 * @author Michael Vorburger.ch
 */
public class JobCoordinatorTest {

    private static final Logger LOG = LoggerFactory.getLogger(JobCoordinatorTest.class);

    private static final RuntimeException JOB_EXCEPTION = new JobException("Job is failed intentionally");

    private static class WaitingCallable implements Callable<List<ListenableFuture<Void>>> {

        public volatile boolean isWaiting = false;
        Object lock = new Object();

        @Override
        public @Nullable List<ListenableFuture<Void>> call() throws Exception {
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

    private static final class JobException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private JobException(String message) {
            super(message);
        }
    }

    private static class TestCallable implements Callable<List<ListenableFuture<Void>>> {

        private final boolean isThrowingException;
        private final int returnedListSize;
        private final @Nullable List<ListenableFuture<Void>> result;
        private final AtomicLong wasTried = new AtomicLong(0);

        TestCallable(boolean isThrowingException, int returnedListSize) {
            this.isThrowingException = isThrowingException;
            this.returnedListSize = returnedListSize;
            if (returnedListSize < 0) {
                this.result = null;
            } else {
                List<ListenableFuture<Void>> nonNullResult = new ArrayList<>(returnedListSize);
                ListenableFuture<Void> future = isThrowingException
                        ? Futures.immediateFailedFuture(JOB_EXCEPTION) : SettableFuture.create();
                for (int i = 0; i < returnedListSize; i++) {
                    nonNullResult.add(i, future);
                }
                this.result = nonNullResult;
            }
        }

        @Override
        public @Nullable List<ListenableFuture<Void>> call() {
            wasTried.incrementAndGet();
            if (isThrowingException && returnedListSize < 0) {
                throw JOB_EXCEPTION;
            }
            return result;
        }

        long getTries() {
            return wasTried.get();
        }
    }

    private static class RollbackTask extends RollbackCallable {
        private final AtomicLong wasTried = new AtomicLong(0);

        @Override
        public @Nullable List<ListenableFuture<Void>> call() throws Exception {
            wasTried.incrementAndGet();
            return Collections.emptyList();
        }

        long getRetries() {
            return wasTried.get();
        }
    }

    public @Rule LogRule logRule = new LogRule();
    public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule(7);
    public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    private static class TestJobCoordinatorImpl extends JobCoordinatorImpl {
        void verifyJobQueueHandlerThreadStopped() {
            assertFalse("obQueueHandler was not stopped", getJobQueueHandlerThread().isAlive());
        }
    }

    private final TestJobCoordinatorImpl jobCoordinator;

    public JobCoordinatorTest() {
        jobCoordinator = new TestJobCoordinatorImpl();
    }

    @After
    public void tearDown() {
        LOG.info("{}", jobCoordinator.toString());
        jobCoordinator.destroy();
        jobCoordinator.verifyJobQueueHandlerThreadStopped();
    }

    @Test
    public void testJobNoExceptionReturnNull() {
        TestCallable testCallable = new TestCallable(false, -1);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getTries()).isEqualTo(1);
        assertExecuteAttempts(0);
    }

    @Test
    public void testJobNoExceptionReturnEmpty() {
        TestCallable testCallable = new TestCallable(false, 0);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getTries()).isEqualTo(1);
        assertExecuteAttempts(0);
    }

    @Test
    public void testSlowJob() {
        WaitingCallable waitingCallable = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName(), waitingCallable);
        assertCreated(1);
        Awaitility.await().until(() -> waitingCallable.isWaiting);
        assertIncomplete(1);
        waitingCallable.stopWaiting();
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertCleared(1);
        assertExecuteAttempts(1);
    }

    @Test
    public void testTwoJobsSameKey() {
        WaitingCallable waitingCallable1 = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName(), waitingCallable1);
        assertThat(jobCoordinator.getCreatedTaskCount()).isEqualTo(1);
        Awaitility.await().until(() -> waitingCallable1.isWaiting);
        WaitingCallable waitingCallable2 = new WaitingCallable();
        jobCoordinator.enqueueJob(getClass().getName(), waitingCallable2);
        assertCreated(2);
        Awaitility.await().until(() -> waitingCallable1.isWaiting);

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
        assertExecuteAttempts(2);
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
        assertExecuteAttempts(1);
    }

    @Test
    public void testJobExceptionThrown() {
        TestCallable testCallable = new TestCallable(true, -1);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertFailed(1);
        assertThat(testCallable.getTries()).isEqualTo(1);
        assertExecuteAttempts(0);
    }

    @Test
    public void testJobFailedFuture() {
        TestCallable testCallable = new TestCallable(true, 1);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertFailed(1);
        assertThat(testCallable.getTries()).isEqualTo(1);
        assertExecuteAttempts(0);
    }

    @Test
    public void testJobRetriesFailedFuture() {
        TestCallable testCallable = new TestCallable(true, 1);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getTries()).isEqualTo(3);
        assertRetry(3);
        assertFailed(1);
        assertExecuteAttempts(0);
    }

    @Test
    public void testJobRetriesExceptionThrown() {
        TestCallable testCallable = new TestCallable(true, -1);
        jobCoordinator.enqueueJob(getClass().getName(), testCallable, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertFailed(1);
        // ====================================================================================
        // NB: Contrary to failed futures, exceptions thrown from call() do NOT cause retries!
        // ====================================================================================
        assertThat(testCallable.getTries()).isEqualTo(1);
        assertRetry(0);
        assertExecuteAttempts(0);
    }

    @Test
    public void testTwoJobsRetriesWhenFailedFuture() {
        TestCallable testCallable1 = new TestCallable(true, 1);
        jobCoordinator.enqueueJob("key1", testCallable1, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable1.getTries()).isEqualTo(3);

        TestCallable testCallable2 = new TestCallable(true, 1);
        jobCoordinator.enqueueJob("key2", testCallable2, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable2.getTries()).isEqualTo(3);

        assertRetry(6);
        assertFailed(2);
        assertExecuteAttempts(0);
    }

    @Test
    public void testJobRetriesWithRollback() {
        TestCallable testCallable = new TestCallable(true, 1);
        RollbackTask rollbackCallable = new RollbackTask();

        jobCoordinator.enqueueJob(getClass().getName(), testCallable, rollbackCallable, 3);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertThat(testCallable.getTries()).isEqualTo(3);
        assertThat(rollbackCallable.getRetries()).isEqualTo(1);
        assertRetry(3);
        assertFailed(1);
        assertExecuteAttempts(0);
    }

    // TODO That this test works is a best of a miracle.. the counters framework has static state :(
    // The OccurenceCounter's clearAllCounters cannot be used, because JobCoordinatorCounters
    // jobs_pending and jobs_incomplete are !isErasable ... we could introduce a new clear, but
    // really, we should write a new counters framework, some day.  Until then, we have to ignore
    // this test, because there is currently no clean way to reset the incompleteTaskCount, therefore
    // it cannot run properly isolate, and will break the other existing tests here.
    @Ignore
    @Test
    public void bug9238CallableListWithNull() {
        // This test didn't fail for https://bugs.opendaylight.org/show_bug.cgi?id=9238 even before its fix
        // but its point is to illustrate the error message in the LOG - before without but now with causing job's key
        Callable<List<ListenableFuture<Void>>> callableListWithNull = () -> Collections.singletonList(null);
        jobCoordinator.enqueueJob(getClass().getName(), callableListWithNull);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(1L));
        assertExecuteAttempts(1);
    }

    private void assertCleared(int count) {
        assertThat(jobCoordinator.getClearedTaskCount()).named("clearedTasks").isEqualTo(count);
    }

    private void assertCreated(int count) {
        assertThat(jobCoordinator.getCreatedTaskCount()).named("createdTasks").isEqualTo(count);
    }

    private void assertIncomplete(int count) {
        assertThat(jobCoordinator.getIncompleteTaskCount()).named("incompleteTasks").isEqualTo(count);
    }

    private void assertPending(int count) {
        assertThat(jobCoordinator.getPendingTaskCount()).named("pendingTasks").isEqualTo(count);
    }

    private void assertFailed(int count) {
        assertThat(jobCoordinator.getFailedJobCount()).named("failedTasks").isEqualTo(count);
    }

    private void assertRetry(int count) {
        assertThat(jobCoordinator.getRetriesCount()).named("retries").isEqualTo(count);
    }

    private void assertExecuteAttempts(int count) {
        assertThat(jobCoordinator.getExecuteAttempts()).named("executeAttempts").isEqualTo(count);
    }
}
