/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * System State.
 *
 * @author Michael Vorburger.ch
 */
public enum SystemState {

    // NB: There is a very similar enum in org.opendaylight.odlparent.bundles4test.SystemState,
    // but this is intentionally copied here instead of referenced from there, because:
    //
    //    (a) we consider odlparent.bundles[4]test an implementation details of infrautils.ready-impl,
    //        which should never be exposed to consumer of the simple infrautils.ready API (impl may change)
    //
    //    (b) we need a 5th new "Changing" state here, which we don't have (and need) there

    BOOTING,

    ACTIVE,

    // TODO uncomment when SystemReadyListener's onSystemIsChanging() will get implemented
    // /** See {@link SystemReadyListener#onSystemIsChanging()}. */
    // CHANGING,

    // TODO this is not implemented, yet; but could be, once we have Robert's hooks into Karaf's shutdown available
    // STOPPING,

    FAILURE

}
