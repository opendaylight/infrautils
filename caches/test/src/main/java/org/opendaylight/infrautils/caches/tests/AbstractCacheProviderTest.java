/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfigBuilder;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Base Unit Test for CacheProvider.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCacheProviderTest {

    public @Rule LogRule logRule = new LogRule();

    protected abstract CacheProvider newCacheProvider();

    protected Cache<Integer, String, Exception> firstCache() {
        return newCacheProvider().createNonDistributedNonTransactionalCache(
            new CacheConfigBuilder<Integer, String, Exception>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .build());
    }


    // TODO create another one, for different types

    @Test
    public void testCreate2() {
        assertThat(firstCache().get(255)).isEqualTo("ff");
    }

    @Test
    public void testToString() {
        // Make sure implementation has a custom toString(), not Object's default
        assertThat(firstCache().toString()).contains("Cache{");
    }

}
