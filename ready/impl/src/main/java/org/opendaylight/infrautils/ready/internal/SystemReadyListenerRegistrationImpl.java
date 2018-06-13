/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyListenerRegistration;

public class SystemReadyListenerRegistrationImpl implements SystemReadyListenerRegistration {

    private final SystemReadyImpl systemReady;
    private final SystemReadyListener listener;

    protected SystemReadyListenerRegistrationImpl(SystemReadyImpl systemReady, SystemReadyListener listener) {
        this.systemReady = systemReady;
        this.listener = listener;
    }

    @Override
    public void close() throws Exception {
        systemReady.unregisterListener(listener);
    }

}
