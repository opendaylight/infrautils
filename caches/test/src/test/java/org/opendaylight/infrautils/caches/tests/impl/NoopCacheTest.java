/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests.impl;

import org.junit.Rule;
import org.opendaylight.infrautils.caches.noop.internal.NoopCacheProvider;
import org.opendaylight.infrautils.caches.tests.AbstractCacheProviderTest;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

public class NoopCacheTest extends AbstractCacheProviderTest {

    public @Rule GuiceRule guice = new GuiceRule(new TestModule(NoopCacheProvider.class));

    @Override
    public void testCacheMonitorPolicyAndStat() {
        // @Ignore this test for the NOOP impl
    }
}
