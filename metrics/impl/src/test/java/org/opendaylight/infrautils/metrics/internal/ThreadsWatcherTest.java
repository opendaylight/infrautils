/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static com.google.common.truth.Truth.assertThat;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;

public class ThreadsWatcherTest {

    @Test
    public void testLogAllThreads() {
        ThreadsWatcher tw = new ThreadsWatcher(ManagementFactory.getThreadMXBean(), 100, Duration.ofNanos(1),
            Duration.ofMinutes(1), Duration.ofMinutes(1));
        tw.start();
        tw.logAllThreads();
        tw.close();
    }

    @Test
    public void testIsConsidered() {
        Instant now = Instant.now();
        ThreadsWatcher tw = new ThreadsWatcher(ManagementFactory.getThreadMXBean(), 100, Duration.ofNanos(1),
            Duration.ofMinutes(1), Duration.ofMinutes(1));

        assertThat(tw.isConsidered(null, now, Duration.ofMinutes(1))).isTrue();
        assertThat(tw.isConsidered(now, now, Duration.ofMinutes(1))).isFalse();
        assertThat(tw.isConsidered(now, now.plusSeconds(30), Duration.ofMinutes(1))).isFalse();
        assertThat(tw.isConsidered(now, now.plusSeconds(60), Duration.ofMinutes(1))).isTrue();
    }
}
