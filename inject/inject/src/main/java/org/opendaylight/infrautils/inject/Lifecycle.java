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
 * Abstract Lifecycle class with convenient default implementations for
 * exception handling, and (minor) check for accidental unnecessary re-start &amp; stop.
 *
 * @author Michael Vorburger
 */
public class Lifecycle implements ILifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(Lifecycle.class);

    private enum State {
        Started, Stopped
    }

    private State state = State.Stopped;

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
        if (state == State.Started) {
            LOG.warn("Lifecycled object already started; ignoring start()");
            return;
        }
        try {
            startWithException();
            state = State.Started;
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

    /**
     * Please override stopWithException() instead of this (here intentionally final) method.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final void stop() throws ModuleSetupRuntimeException {
        if (state == State.Stopped) {
            LOG.warn("Lifecycled object already stopped; ignoring stop()");
            return;
        }
        try {
            stopWithException();
            state = State.Stopped;
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

}
