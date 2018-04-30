/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.opendaylight.infrautils.testutils.Partials;

/**
 * {@link ScheduledExecutorService} implementation suitable for testing code relying on its schedule methods.
 *
 * @author Michael Vorburger.ch
 */
public abstract class TestableScheduledExecutorService implements ScheduledExecutorService {

    public static TestableScheduledExecutorService newInstance() {
        return Partials.newPartial(TestableScheduledExecutorService.class);
    }

    private volatile @Nullable ScheduledFutureTestImpl<?> scheduledFutureTestImpl;

    @Override
    @Nullable
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (scheduledFutureTestImpl != null) {
            throw new IllegalStateException(getClass().getSimpleName() + " currently only supported 1 scheduled task");
        }
        scheduledFutureTestImpl = new ScheduledFutureTestImpl<>(command, null);
        return scheduledFutureTestImpl;
    }

    public void runScheduled() {
        scheduledFutureTestImpl.run();
    }
}
