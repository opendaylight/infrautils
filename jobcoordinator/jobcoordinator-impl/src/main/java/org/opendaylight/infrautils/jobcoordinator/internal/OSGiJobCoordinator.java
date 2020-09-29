/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.internal;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinator;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinatorMonitor;
import org.opendaylight.infrautils.jobcoordinator.RollbackCallable;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Beta
@Component(immediate = true)
// FIXME: integrate with JobCoordinatorImpl when we have OSGi DS with constructor injection
@SuppressFBWarnings(value = { "NP_STORE_INTO_NONNULL_FIELD", "UWF_NULL_FIELD" },
        justification = "SpotBugs does not grok @NonNullByDefault and @Nullable")
public final class OSGiJobCoordinator implements JobCoordinator, JobCoordinatorMonitor {
    @Reference
    @Nullable MetricProvider metricProvider = null;

    private @Nullable JobCoordinatorImpl delegate = null;

    @Override
    public long getClearedTaskCount() {
        return delegate().getClearedTaskCount();
    }

    @Override
    public long getCreatedTaskCount() {
        return delegate().getCreatedTaskCount();
    }

    @Override
    public long getIncompleteTaskCount() {
        return delegate().getIncompleteTaskCount();
    }

    @Override
    public long getPendingTaskCount() {
        return delegate().getPendingTaskCount();
    }

    @Override
    public long getFailedJobCount() {
        return delegate().getFailedJobCount();
    }

    @Override
    public long getRetriesCount() {
        return delegate().getRetriesCount();
    }

    @Override
    public void enqueueJob(Object key, Callable<List<? extends ListenableFuture<?>>> mainWorker,
            RollbackCallable rollbackWorker, int maxRetries) {
        delegate().enqueueJob(key, mainWorker, rollbackWorker, maxRetries);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }

    @Activate
    void activate() {
        delegate = new JobCoordinatorImpl(verifyNotNull(metricProvider));
    }

    @Deactivate
    void deactivate() {
        delegate.destroy();
        delegate = null;
    }

    private JobCoordinatorImpl delegate() {
        return verifyNotNull(delegate);
    }
}
