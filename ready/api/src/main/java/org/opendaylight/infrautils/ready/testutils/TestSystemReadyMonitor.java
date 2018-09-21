/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.testutils;

import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;

/**
 * {@link SystemReadyMonitor} implementation suitable for very simple unit
 * tests. Its system state is always ready, there are never any failures, and
 * any {@link SystemReadyListener} registrations immediately get their
 * {@link SystemReadyListener#onSystemBootReady} invoked.
 *
 * <p>Do not use this for the Guice-based component tests.  (TODO Must some upstream
 * the ReadyWiring, PostFullSystemInjectionListener, SystemReadyBaseImpl stuff
 * from https://github.com/vorburger/opendaylight-simple/ - that's the equivalent
 * to this for Guice environments like component tests or light standalone
 * environments.)
 *
 * @author Michael Vorburger.ch
 */
public class TestSystemReadyMonitor implements SystemReadyMonitor {

    @Override
    public void registerListener(SystemReadyListener listener) {
        listener.onSystemBootReady();
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
