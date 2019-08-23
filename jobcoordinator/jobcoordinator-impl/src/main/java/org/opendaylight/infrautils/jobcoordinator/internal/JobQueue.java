/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A queue which holds job entries and the current running job.
 */
public class JobQueue {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private final String queueId = "Q" + ID_GENERATOR.getAndIncrement();
    private final Queue<JobEntry> jobQueue = new ConcurrentLinkedQueue<>();
    private volatile @Nullable JobEntry executingEntry;
    private double movingAverage = -1D;
    private int pendingJobCount;
    private int finishedJobCount;

    @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "TYPE_USE and SpotBugs")
    public JobQueue() {

    }

    public void addEntry(JobEntry entry) {
        jobQueue.add(entry);
        this.pendingJobCount++;
    }

    public boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    public @Nullable JobEntry poll() {
        return jobQueue.poll();
    }

    public @Nullable JobEntry getExecutingEntry() {
        return executingEntry;
    }

    public void setExecutingEntry(@Nullable JobEntry executingEntry) {
        this.executingEntry = executingEntry;
    }


    public String getQueueId() {
        return queueId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("executing", executingEntry).add("queue", jobQueue).toString();
    }

    public void onJobFinished(long timeTaken) {
        this.finishedJobCount++;
        this.pendingJobCount--;
        this.movingAverage = getMovingAverage(this.movingAverage,timeTaken);
    }

    public int getPendingJobCount() {
        return pendingJobCount;
    }

    public int getFinishedJobCount() {
        return finishedJobCount;
    }

    public double getJobQueueMovingAverageExecutionTime() {
        return this.movingAverage;
    }

    private static double getMovingAverage(@Var double st, double yt) {

        return st <= 0D ? yt : 0.01D * yt + 0.99D * st;
    }
}
