/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

public final class JcState {

    private final int pendingJobCount;
    private final int finishedJobCount;
    private final double jobExecutionMovingAverage;

    /**
     *
     * {@link JcState} is used when the user requests for the JcStatus,
     * a map is created with the hashkey and the jobQueue info(only pendingJobCount, finishedJobCount and
     * jobExecutionMovingAverage). So this jobQueue info is stored in JcState Object.
     *
     */

    public JcState(int pendingJobCount, int finishedJobCount, double jobExecutionMovingAverage) {
        this.pendingJobCount = pendingJobCount;
        this.finishedJobCount = finishedJobCount;
        this.jobExecutionMovingAverage = jobExecutionMovingAverage;
    }

    public int getPendingJobCount() {
        return pendingJobCount;
    }

    public int getFinishedJobCount() {
        return finishedJobCount;
    }

    public double getJobExecutionMovingAverage() {
        return jobExecutionMovingAverage;
    }

    @Override
    public String toString() {
        return "JcState{"
                + ", pendingJobCount=" + pendingJobCount
                + ", finishedJobCount=" + finishedJobCount
                + ", jobExecutionMovingAverage=" + jobExecutionMovingAverage
                + '}';
    }
}
