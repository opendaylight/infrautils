/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A queue which holds job entries and the current running job.
 */
public class JobQueue {
    private final Queue<JobEntry> jobQueue = new ArrayDeque<JobEntry>();
    private volatile JobEntry executingEntry;

    public void addEntry(JobEntry entry) {
        jobQueue.add(entry);
    }

    public boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    public JobEntry poll() {
        return jobQueue.poll();
    }

    public JobEntry getExecutingEntry() {
        return executingEntry;
    }

    public void setExecutingEntry(JobEntry executingEntry) {
        this.executingEntry = executingEntry;
    }
}
