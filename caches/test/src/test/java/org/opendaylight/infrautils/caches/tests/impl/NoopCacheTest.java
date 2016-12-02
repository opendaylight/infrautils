/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests.impl;

import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.noop.internal.NoopCacheProvider;
import org.opendaylight.infrautils.caches.tests.AbstractCacheProviderTest;

/**
 * Test the NOOP Cache.
 *
 * @author Michael Vorburger.ch
 */
public class NoopCacheTest extends AbstractCacheProviderTest {

    @Override
    protected CacheProvider newCacheProvider() {
        return new NoopCacheProvider();
    }

}
