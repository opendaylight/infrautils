/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.order;

/**
 * Service to register {@link FunctionalityReady} markers.
 *
 * @author Michael Vorburger.ch
 */
public interface FunctionalityReadyNotifier {

    /**
     * Unregister a {@link FunctionalityReady} marker.
     *
     * @param markerInterface
     *            the marker FunctionalityReady marker interface
     * @return the {@link FunctionalityReadyRegistration} which allows to
     *         {@link FunctionalityReadyRegistration#unregister()} the marker
     *
     * @see FunctionalityReady
     */
    <T extends FunctionalityReady> FunctionalityReadyRegistration<T> register(Class<T> markerInterface);

}
