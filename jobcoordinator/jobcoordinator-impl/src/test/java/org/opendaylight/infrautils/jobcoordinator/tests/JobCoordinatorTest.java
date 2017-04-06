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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Unit test for JobCoordinator.
 *
 * @author Michael Vorburger.ch
 */
public class JobCoordinatorTest {

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
    // public static @ClassRule RunUntilFailureClassRule classRepeater = new RunUntilFailureClassRule();
    // public @Rule RunUntilFailureRule repeater = new RunUntilFailureRule(classRepeater);

    private JobCoordinatorImpl jobCoordinator;

    @Before
    public void setUp() {
        jobCoordinator = new JobCoordinatorImpl();
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

    // TODO expand this - significantly - until JobCoordinatorImpl has 100% functional as well as line coverage:
    //   * all permutations of above with all enqueueJob() variants - rollbackWorker & retries
    //   * keys! Jobs with the same key are run sequentially. Jobs with different keys are run in parallel.
    //   * SettableFuture set(), setException() & cancel(true) & cancel(false) - for all permutations

}
