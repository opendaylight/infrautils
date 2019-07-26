/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

/**
 * A registration of a diagstatus "Service".
 *
 * <p>Not to be confused with an org.osgi.framework.ServiceRegistration, which this has nothing to do with.
 *
 * @author Michael Vorburger.ch
 */
public interface ServiceRegistration {
    /**
     * Unregisters a service.
     *
     * @throws IllegalStateException If this {@code ServiceRegistration} object has already been unregistered.
     */
    void unregister();
}
