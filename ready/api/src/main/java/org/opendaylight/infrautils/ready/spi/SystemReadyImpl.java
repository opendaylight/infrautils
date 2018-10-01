/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.spi;

import static org.opendaylight.infrautils.ready.SystemState.ACTIVE;
import static org.opendaylight.infrautils.ready.SystemState.BOOTING;
import static org.opendaylight.infrautils.ready.SystemState.FAILURE;

import com.google.common.base.Throwables;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SystemReadyMonitor} implementation for a "simple" (standalone, Java SE) environment.
 * Also used as the base class for the Karaf/OSGi specific implementation.
 *
 * @author Michael Vorburger.ch, based on code from myself, Tom (concurrency) &amp; Faseela in KarafSystemReadyImpl
 */
public class SystemReadyImpl implements SystemReadyMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReadyImpl.class);

    private final Queue<SystemReadyListener> listeners = new ConcurrentLinkedQueue<>();
    private final AtomicReference<SystemState> currentSystemState = new AtomicReference<>(BOOTING);
    private final AtomicReference<Throwable> currentSystemFailureCause = new AtomicReference<>();

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void registerListener(SystemReadyListener listener) {
        SystemState state;
        synchronized (listeners) {
            state = currentSystemState.get();
            if (state == BOOTING) {
                listeners.add(listener);
            }
        }

        if (state == ACTIVE) {
            try {
                listener.onSystemBootReady();
            } catch (Exception e) {
                LOG.error("SystemReadyListener.onSystemBootReady() threw Exception; "
                        + "but state was already ACTIVE, going back to FAILURE now", e);
                currentSystemState.set(FAILURE);
                currentSystemFailureCause.set(e);
                // Do not re-throw.
            }
        }
    }

    @Override
    public SystemState getSystemState() {
        return currentSystemState.get();
    }

    @Override
    public String getFailureCause() {
        return Optional.ofNullable(currentSystemFailureCause.get())
                .map(throwable -> Throwables.getStackTraceAsString(throwable)).orElse("");
    }

    @SuppressWarnings("checkstyle:IllegalCatch") // below
    public void ready() {
        SystemReadyListener[] toNotify;
        synchronized (listeners) {
            toNotify = listeners.toArray(new SystemReadyListener[listeners.size()]);
            currentSystemState.set(ACTIVE);
        }
        LOG.info("System ready; AKA: Aye captain, all warp coils are now operating at peak efficiency! [M.]");

        if (toNotify.length > 0) {
            LOG.info("Now notifying all its registered SystemReadyListeners...");
        }

        try {
            for (SystemReadyListener element : toNotify) {
                element.onSystemBootReady();
            }
        } catch (RuntimeException throwable) {
            // It's exceptionally OK to catch RuntimeException here,
            // because we do want to set the currentFullSystemStatus
            LOG.error("Boot failed; not all SystemReadyListeners were not called, SystemState FAILURE", throwable);
            setSystemState(FAILURE);
            setSystemFailureCause(throwable);
            // and now we do re-throw it!
            throw throwable;
        } catch (Exception e) {
            LOG.error("SystemReadyListener.onSystemBootReady() threw Exception; "
                    + "other SystemReadyListeners not called; SystemState FAILURE", e);
            setSystemState(FAILURE);
            setSystemFailureCause(e);
            // really no point in re-throwing it
        }
    }

    protected void setSystemState(SystemState state) {
        currentSystemState.set(state);
    }

    protected void setSystemFailureCause(Throwable cause) {
        currentSystemFailureCause.set(cause);
    }
}
