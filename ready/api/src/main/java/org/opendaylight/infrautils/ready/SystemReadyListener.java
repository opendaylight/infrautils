/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

/**
 * Listener which can get notified when the system is "fully ready".
 *
 * @see SystemReadyService
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface SystemReadyListener {

    void onSystemReady();

}
