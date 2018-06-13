/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * Listener which can get notified when the system state is changing.
 *
 * @see SystemReadyMonitor
 *
 * @author Michael Vorburger.ch
 */
public interface SystemReadyListener {

    /**
     * Called back on system state change.
     * @param systemState
     *   Current system state.
     */
    void onSystemStateChange(SystemState systemState);

}
