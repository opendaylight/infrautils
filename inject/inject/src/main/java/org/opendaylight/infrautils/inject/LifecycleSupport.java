/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class for {@link Lifecycle}.  Provides a convenient base implementation
 * including exception handling and check for accidental unnecessary re-start &amp; stop.
 * Subclasses should override {@link #startWithException()} and/or {@link #stopWithException()},
 * as required.
 *
 * @author Michael Vorburger
 */
public class LifecycleSupport implements Lifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(LifecycleSupport.class);

    private enum State {
        STARTED, STOPPED
    }

    private State state = State.STOPPED;

    protected void startWithException() throws Exception {
    }

    protected void stopWithException() throws Exception {
    }

    /**
     * Please override startWithException() instead of this (here intentionally final) method.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final synchronized void start() throws ModuleSetupRuntimeException {
        if (state == State.STARTED) {
            LOG.warn("Lifecycled object already started; ignoring start()");
            return;
        }
        try {
            startWithException();
            state = State.STARTED;
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

    /**
     * Please override stopWithException() instead of this (here intentionally final) method.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final synchronized void stop() throws ModuleSetupRuntimeException {
        if (state == State.STOPPED) {
            LOG.warn("Lifecycled object already stopped; ignoring stop()");
            return;
        }
        try {
            stopWithException();
            state = State.STOPPED;
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

    @Override
    public boolean isRunning() {
        return state == State.STARTED;
    }

}
