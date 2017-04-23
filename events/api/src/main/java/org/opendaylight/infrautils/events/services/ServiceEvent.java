/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.services;

import java.util.Optional;
import org.immutables.value.Value;

/**
 * Event related to a dynamic service, such as the OSGi service registry.
 * In test environments, this may be emitted by a dependency injection framework such as e.g. Guice as well.
 *
 * @param <T> service's Java type
 *
 * @author Michael Vorburger.ch
 */
public interface ServiceEvent<T> {

    Class<T> getServiceType();

    @Value.Parameter(false)
    Optional<String> filter();

}

