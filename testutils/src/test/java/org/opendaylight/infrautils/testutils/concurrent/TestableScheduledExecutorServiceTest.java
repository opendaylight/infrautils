/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

/**
 * Unit test for {@link TestableScheduledExecutorService}.
 *
 * @author Michael Vorburger.ch
 */
public class TestableScheduledExecutorServiceTest {

    @Test
    public void testTestableScheduledExecutorService() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        TestableScheduledExecutorService scheduler = TestableScheduledExecutorService.newInstance();
        ScheduledFuture<?> future = scheduler.schedule(() -> called.set(true), 0, TimeUnit.NANOSECONDS);
        assertThat(future.isDone()).isFalse();
        assertThat(future.isCancelled()).isFalse();
        assertThat(called.get()).isFalse();
        scheduler.runScheduled();
        assertThat(called.get()).isTrue();
        assertThat(future.get(1, SECONDS)).isNull();
    }

}
