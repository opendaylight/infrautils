/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Apps can register an implementation of this interface in the OSGi service registry
 * if they want to expose their live status.
 *
 * @author Faseela K
 */
public interface ServiceStatusProvider {

    /**
     * Called back whenever a query comes for the current status of
     * the registered services.
     *
     * @return current ServiceDescriptor (never null)
     */
    @NonNull ServiceDescriptor getServiceDescriptor();
}
