/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.micrometer.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.infrautils.metrics.Timer;
import org.opendaylight.infrautils.utils.function.CheckedCallable;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;

/**
 * Adapts Infrautils' Metrics to Micrometer Timer.
 *
 * @author Michael Vorburger.ch
 */
public class TimerAdapter implements Timer {

    private final io.micrometer.core.instrument.Timer micrometerTimer;

    public TimerAdapter(io.micrometer.core.instrument.Timer timer) {
        this.micrometerTimer = timer;
    }

    @Override
    public void close() {
        micrometerTimer.close();
    }

    // TODO The following code is copy/pasted from MetricProviderImpl, but really should be shared with it..

    @Override
    @SuppressWarnings({ "unchecked", "checkstyle:IllegalCatch" })
    public <T, E extends Exception> T time(CheckedCallable<T, E> event) throws E {
        try {
            return micrometerTimer.recordCallable(event::call);
        } catch (Exception e) {
            throw (E) e;
        }
    }

    @Override
    @SuppressWarnings({ "checkstyle:IllegalCatch", "checkstyle:AvoidHidingCauseException", "unchecked" })
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE") // getCause() will be Exception not Throwable
    public <E extends Exception> void time(CheckedRunnable<E> event) throws E {
        try {
            micrometerTimer.record(() -> {
                try {
                    event.run();
                } catch (Exception exception) {
                    throw new InternalRuntimeException(exception);
                }
            });
        } catch (InternalRuntimeException e) {
            throw (E) e.getCause();
        }
    }

    private static class InternalRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InternalRuntimeException(Exception exception) {
            super(exception);
        }
    }
}
