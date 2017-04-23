/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator;

/**
 * Monitor for {@link JobCoordinator} metrics.
 */
public interface JobCoordinatorMonitor {

    /**
     * Returns the cleared task count.
     */
    long getClearedTaskCount();

    /**
     * Returns the created task count.
     */
    long getCreatedTaskCount();

    /**
     * Returns the incomplete task count.
     */
    long getIncompleteTaskCount();

    /**
     * Returns the pending task count.
     */
    long getPendingTaskCount();

    /**
     * Returns the failed jobs count.
     */
    long getFailedJobCount();

    /**
     * Returns the retry jobs count.
     */
    long getRetriesCount();

}
