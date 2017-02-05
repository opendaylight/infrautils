/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;

/**
 * Unit test for JobCoordinator.
 *
 * @author Michael Vorburger
 */
public class JobCoordinatorTest {

    private static class TestCallable implements Callable<List<ListenableFuture<Void>>> {

        boolean wasCalled = false;

        @Override
        public List<ListenableFuture<Void>> call() {
            wasCalled = true;
            return null;
        }
    }

    // public static @ClassRule RunUntilFailureClassRule classRepeater = new
    // RunUntilFailureClassRule();
    // public @Rule RunUntilFailureRule repeater = new
    // RunUntilFailureRule(classRepeater);

    @Test
    public void testJobCoordinatorUsingPendingTasksCounter() {
        JobCoordinatorImpl jobCoordinator = new JobCoordinatorImpl();
        TestCallable testCallable = new TestCallable();
        jobCoordinator.enqueueJob(getClass().getName().toString(), testCallable);
        Awaitility.await().until(() -> jobCoordinator.getIncompleteTaskCount(), is(0L));
        assertTrue(testCallable.wasCalled);
    }
}