/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.jobcoordinator.workaround;

import org.opendaylight.infrautils.jobcoordinator.JobCoordinator;
import org.opendaylight.infrautils.jobcoordinator.JobCoordinatorMonitor;
import org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Bundle Activator which registers JobCoordinatorImpl as OSGi Service.
 *
 * <p>It also gives a static access to the JobCoordinator, which is required by
 * DataStoreJobCoordinator in genius; because that could be used by other bundles
 * BEFORE Blueprint had a change to kick in and register the service.
 *
 * @deprecated Do *NOT* use this, it's temporary just until we remove
 *             DataStoreJobCoordinator in genius, and WILL be removed.
 *
 * @author Michael Vorburger.ch
 */
@Deprecated
public class Activator implements BundleActivator {

    // static just so that we can access them from the static methods (that's the whole point of this)
    private static final JobCoordinatorImpl INSTANCE = new JobCoordinatorImpl();

    public static JobCoordinator getJobCoordinator() {
        return INSTANCE;
    }

    public static JobCoordinatorMonitor getJobCoordinatorMonitor() {
        return INSTANCE;
    }

    private ServiceRegistration<JobCoordinator> jobCoordinatorServiceRegistration;
    private ServiceRegistration<JobCoordinatorMonitor> jobCoordinatorMonitorServiceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        jobCoordinatorServiceRegistration = context.registerService(JobCoordinator.class, INSTANCE, null);
        jobCoordinatorMonitorServiceRegistration = context.registerService(JobCoordinatorMonitor.class, INSTANCE, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        INSTANCE.destroy();
        if (jobCoordinatorServiceRegistration != null) {
            jobCoordinatorServiceRegistration.unregister();
        }

        if (jobCoordinatorMonitorServiceRegistration != null) {
            jobCoordinatorMonitorServiceRegistration.unregister();
        }
    }

}
