/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.testutils;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.infrautils.ready.testutils.TestSystemReadyMonitor.Behaviour.IMMEDIATE;

import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;

/**
 * {@link SystemReadyMonitor} implementation suitable for tests.
 * The {@link Behaviour} selects what it actually does.
 *
 * @author Michael Vorburger.ch
 */
public class TestSystemReadyMonitor implements SystemReadyMonitor {

    public enum Behaviour {
        /**
         * The system state is always ACTIVE, there are never any failures, and
         * any {@link SystemReadyListener} registrations *NEVER* get their
         * {@link SystemReadyListener#onSystemBootReady} invoked.
         */
        NEVER,

        /**
         * The system state is always ACTIVE, there are never any failures, and
         * any {@link SystemReadyListener} registrations immediately get their
         * {@link SystemReadyListener#onSystemBootReady} invoked.
         */
        IMMEDIATE

        /* TODO add * to make this JavaDoc
         * The system state is initially BOOTING, there are never any failures, and
         * any {@link SystemReadyListener} registrations get their
         * {@link SystemReadyListener#onSystemBootReady} invoked when the test
         * explicitly invokes the {@link #ready()} method, after which the
         * system state turns to ACTIVE.
          */
        // TODO EXPLICIT
        /* Must upstream the ReadyWiring, PostFullSystemInjectionListener, SystemReadyBaseImpl stuff
         * from https://github.com/vorburger/opendaylight-simple/ - that's provides the EXPLICIT,
         * which is the equivalent to this for Guice environments like component tests, or
         * even light standalone (non-test) environments.
         */
    }

    private final Behaviour behaviour;

    public TestSystemReadyMonitor(Behaviour behaviour) {
        this.behaviour = requireNonNull(behaviour, "behaviour");
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void registerListener(SystemReadyListener listener) {
        if (behaviour.equals(IMMEDIATE)) {
            try {
                listener.onSystemBootReady();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "SystemReadyListener.onSystemBootReady() threw Exception; rethrowing to fail test", e);
            }
        }
    }

    @Override
    public SystemState getSystemState() {
        return SystemState.ACTIVE;
    }

    @Override
    public String getFailureCause() {
        return "";
    }
}
