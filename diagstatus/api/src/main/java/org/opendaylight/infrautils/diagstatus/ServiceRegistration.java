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
 * <p>Not to be confused with an {@code org.osgi.framework.ServiceRegistration}, which this has nothing to do with.
 *
 * @author Michael Vorburger.ch
 */
public interface ServiceRegistration extends AutoCloseable {
    /**
     * Report the status of the registered service.
     *
     * @param serviceDescriptor description of the service state
     * @throws NullPointerException if {@code serviceDescriptor} is {@code null}
     * @throws IllegalStateException if this registration has been {@link #close()}d
     */
    void report(ServiceDescriptor serviceDescriptor);

    @Override
    void close();
}
