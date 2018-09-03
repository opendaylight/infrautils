/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyNotifier;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyRegistration;
import org.ops4j.pax.cdi.api.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link FunctionalityReadyNotifier}.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Service
public class FunctionalityReadyNotifierImpl implements FunctionalityReadyNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionalityReadyNotifierImpl.class);

    private final BundleContext bundleContext;

    @Inject
    public FunctionalityReadyNotifierImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    // synchronized because of the check below to make sure only 1 is ever registered
    public synchronized <T extends FunctionalityReady> FunctionalityReadyRegistration<T> register(
            Class<T> markerInterface) {

        requireNonNull(markerInterface, "markerInterface == null");
        checkArgument(markerInterface.isInterface(), "markerInterface is not an interface (cannot be a class");

        try {
            if (bundleContext.getAllServiceReferences(markerInterface.getName(), null).length > 0) {
                throw new IllegalStateException("Already a FunctionalityReady registration for: " + markerInterface);
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("InvalidSyntaxException should never happen for 'null' filter?!", e);
        }

        T uselessInstance = ProxyUtil.newInstance(markerInterface);
        ServiceRegistration<T> registration = bundleContext.registerService(markerInterface, uselessInstance, null);

        LOG.info("FunctionalityReady now registered as (pseudo) OSGi service: {}", markerInterface.getName());

        return () -> {
            try {
                registration.unregister();
            } catch (IllegalStateException e) {
                LOG.debug("org.osgi.framework.ServiceRegistration.unregister() failed", e);
            }
        };
    }

}
