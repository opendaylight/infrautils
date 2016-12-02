/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.datastore;

import org.opendaylight.mdsal.binding.api.DataBroker;

/**
 * DataBroker which caches.
 *
 * <p>The underlying cache is administered and monitored using the CachesMonitor
 * and the CachesAdministration - like all other (non-datastore) caches.
 *
 * @author Michael Vorburger
 */
public interface CachingDataBrokerProvider extends DataBroker {

    CachingDataBroker getCachingDataBroker(Class<?> callerClass, String name);

}
