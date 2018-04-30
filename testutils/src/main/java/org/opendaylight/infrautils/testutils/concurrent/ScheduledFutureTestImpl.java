/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * ScheduledFuture implementation suitable for use by {@link TestableScheduledExecutorService} in tests.
 *
 * @author Michael Vorburger.ch
 */
// We have to implement compareTo() but don't have a an intelligent way to implement equals() or hashCode()
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
// intentionally just package-local
class ScheduledFutureTestImpl<V> extends FutureTask<V> implements ScheduledFuture<V> {

    ScheduledFutureTestImpl(Callable<V> callable) {
        super(callable);
    }

    // The result *IS* @Nullable but because FindBugs does not have a concept like Eclipse' external null annotations,
    // there is really no way to teach it about null related metadata about external code such as the JDK.
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    ScheduledFutureTestImpl(Runnable runnable, @Nullable V result) {
        super(runnable, result);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed other) {
        // inspired by private class java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask
        if (other == this) {
            return 0;
        }
        long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
        return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }

}
