/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import java.util.concurrent.ConcurrentLinkedQueue;

public class JobQueue {
    private final ConcurrentLinkedQueue<JobEntry> waitingEntries = new ConcurrentLinkedQueue<>();
    private volatile JobEntry executingEntry;

    public void addEntry(JobEntry entry) {
        waitingEntries.add(entry);
    }

    public ConcurrentLinkedQueue<JobEntry> getWaitingEntries() {
        return waitingEntries;
    }

    public JobEntry getExecutingEntry() {
        return executingEntry;
    }

    public void setExecutingEntry(JobEntry executingEntry) {
        this.executingEntry = executingEntry;
    }
}
