/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class for {@link Lifecycle}. Provides a convenient base
 * implementation including correct thread safety, exception handling and check
 * for accidental unnecessary re-start &amp; stop. Subclasses should override
 * {@link #startWithException()} and/or {@link #stopWithException()}, as
 * required.
 *
 * @author Michael Vorburger (with guidance re. AtomicReference from Tom Pantelis)
 */
public class LifecycleSupport implements Lifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(LifecycleSupport.class);

    private enum State {
        STARTED, STOPPED
    }

    private AtomicReference<State> state = new AtomicReference<>(State.STOPPED);

    protected void startWithException() throws Exception {
    }

    protected void stopWithException() throws Exception {
    }

    /**
     * Please override startWithException() instead of this (here intentionally final) method.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final void start() throws ModuleSetupRuntimeException {
        if (state.compareAndSet(State.STOPPED, State.STARTED)) {
            try {
                startWithException();
            } catch (Exception e) {
                throw new ModuleSetupRuntimeException(e);
            }
        } else {
            LOG.warn("Lifecycled object already started; ignoring start()");
        }
    }

    /**
     * Please override stopWithException() instead of this (here intentionally final) method.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final void stop() throws ModuleSetupRuntimeException {
        if (state.compareAndSet(State.STARTED, State.STOPPED)) {
            try {
                stopWithException();
            } catch (Exception e) {
                throw new ModuleSetupRuntimeException(e);
            }
        } else {
            LOG.warn("Lifecycled object already stopped; ignoring stop()");
        }
    }

    @Override
    public boolean isRunning() {
        return state.get() == State.STARTED;
    }

}
