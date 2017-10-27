/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import java.util.concurrent.atomic.AtomicLong;

class JobCoordinatorCounters {
    private final AtomicLong jobsCreated = new AtomicLong();
    private final AtomicLong jobsCleared = new AtomicLong();
    private final AtomicLong jobsPending = new AtomicLong();
    private final AtomicLong jobsIncomplete = new AtomicLong();
    private final AtomicLong jobsFailed = new AtomicLong();
    private final AtomicLong jobsRetriesForFailure = new AtomicLong();
    private final AtomicLong jobExecuteAttempts = new AtomicLong();

    AtomicLong jobsCreated() {
        return jobsCreated;
    }

    AtomicLong jobsCleared() {
        return jobsCleared;
    }

    AtomicLong jobsPending() {
        return jobsPending;
    }

    AtomicLong jobsIncomplete() {
        return jobsIncomplete;
    }

    AtomicLong jobsFailed() {
        return jobsFailed;
    }

    AtomicLong jobsRetriesForFailure() {
        return jobsRetriesForFailure;
    }

    AtomicLong jobExecuteAttempts() {
        return jobExecuteAttempts;
    }
}
