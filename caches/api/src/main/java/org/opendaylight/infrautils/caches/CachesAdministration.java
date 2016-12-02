/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

/**
 * Service for cache configuration admin.
 *
 * <p>Allows to change settings at run-time.
 *
 * @author Michael Vorburger.ch
 */
public interface CachesAdministration {

    // TODO learn how other stuff is configured in ODL.. CSS admin is dead? OSGi config? Just DS?

    void evictAll();

}
