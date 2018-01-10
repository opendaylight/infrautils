/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import java.time.Duration;
import org.junit.Test;

public class ThreadsWatcherTest {

    @Test
    public void testLogAllThreads() {
        ThreadsWatcher threadsWatcher = new ThreadsWatcher(100, Duration.ofNanos(1));
        threadsWatcher.start();
        threadsWatcher.logAllThreads();
        threadsWatcher.close();
    }
}
