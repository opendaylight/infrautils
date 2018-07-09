/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * This enum represents System State.
 * Allowed state transitions:
 * BOOTING -&gt; ACTIVE
 * BOOTING -&gt; FAILURE

 * @author Michael Vorburger.ch
 */
public enum SystemState {

    /**
     * System core services are being initialized.
     */
    BOOTING,

    /**
     * System core services are active, available for clients to use.
     * This state is terminal, no transition from this state is allowed.
     */
    ACTIVE,

    /**
     * System is in failed state after failed initialization procedure.
     * This state is terminal, no transition from this state is allowed.
     */
    FAILURE,

}
