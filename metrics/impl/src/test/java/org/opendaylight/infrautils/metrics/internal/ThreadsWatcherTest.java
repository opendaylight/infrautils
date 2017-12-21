/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import org.junit.Test;

public class ThreadsWatcherTest {

    @Test
    public void testLogAllThreads() {
        ThreadsWatcher threadsWatcher = new ThreadsWatcher(100, 1, NANOSECONDS);
        threadsWatcher.logAllThreads();
        threadsWatcher.close();
    }
}
