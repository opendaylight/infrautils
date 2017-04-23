/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import org.opendaylight.infrautils.counters.api.OccurenceCounter;

enum JobCoordinatorCounters {
    jobs_created, jobs_cleared, jobs_pending(true), jobs_incomplete(true), jobs_failed,
    jobs_retries_for_failure;

    private OccurenceCounter counter;

    JobCoordinatorCounters() {
        counter = new OccurenceCounter(getClass().getSimpleName(), name(), name());
    }

    JobCoordinatorCounters(boolean isState) {
        counter = new OccurenceCounter(getClass().getSimpleName(), "dsjcc", name(), name(), false, null, true, true);
    }

    public void inc() {
        counter.inc();
    }

    public void dec() {
        counter.dec();
    }

    public long get() {
        return counter.get();
    }
}
