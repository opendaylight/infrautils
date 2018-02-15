/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.order;

/**
 * Registration of a {@link FunctionalityReady}, used to {@link #unregister()}
 * it again.
 *
 * <p>Intentionally not using org.osgi.framework.ServiceRegistration, as the
 * ServiceReference is irrelevant, there are no properties (we would want any
 * multiple instances to be distinguished by static type, not properties, to make
 * it easier to integrate with other Dependency Injection frameworks), and to avoid
 * an otherwise unnecessary dependency of ready-api to the OSGi Core API.
 *
 * @author Michael Vorburger.ch
 */
public interface FunctionalityReadyRegistration<S> {

    void unregister();

}
