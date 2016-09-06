/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support class for {@link Lifecycle}. Provides a convenient base
 * implementation including correct thread safety, exception handling and check
 * for accidental unnecessary re-start &amp; stop. Subclasses must implement
 * {@link #start()} &amp; {@link #stop()}.
 *
 * @author Michael Vorburger (with guidance re. AtomicReference from Tom Pantelis)
 */
public abstract class AbstractLifecycle implements Lifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLifecycle.class);

    private enum State {
        STARTED, STOPPED
    }

    private AtomicReference<State> state = new AtomicReference<>(State.STOPPED);

    protected abstract void start() throws Exception;

    protected abstract void stop() throws Exception;

    /**
     * Please implement {@link #start()} instead of overriding this (here intentionally final) method.
     */
    @Override
    @PostConstruct // NOTE: @PostConstruct is *NOT* inherited from interface, so must be here
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final void init() throws ModuleSetupRuntimeException {
        if (state.compareAndSet(State.STOPPED, State.STARTED)) {
            try {
                start();
            } catch (Exception e) {
                throw new ModuleSetupRuntimeException(e);
            }
        } else {
            LOG.warn("Lifecycled object already started; ignoring start()");
        }
    }

    /**
     * Please implement {@link #stop()} instead of overriding this (here intentionally final) method.
     */
    @Override
    @PreDestroy // NOTE: @PostConstruct is *NOT* inherited from interface, so must be here
    @SuppressWarnings("checkstyle:IllegalCatch")
    public final void destroy() throws ModuleSetupRuntimeException {
        if (state.compareAndSet(State.STARTED, State.STOPPED)) {
            try {
                stop();
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
