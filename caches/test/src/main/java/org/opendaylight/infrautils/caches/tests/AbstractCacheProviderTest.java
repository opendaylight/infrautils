/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
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

    private Cache<Integer, String, Exception> firstCache() {
        return newCacheProvider().createNonDistributedNonTransactionalCache(
            new CacheConfigBuilder<Integer, String, Exception>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .build());
    }

    private Cache<Double, BigDecimal, Exception> secondCache() {
        return newCacheProvider().createNonDistributedNonTransactionalCache(
            new CacheConfigBuilder<Double, BigDecimal, Exception>()
                .cacheFunction(d -> BigDecimal.valueOf(d))
                .anchor(this)
                .build());
    }

    private Cache<String, Object, Exception> badNullCache() {
        return newCacheProvider().createNonDistributedNonTransactionalCache(
            new CacheConfigBuilder<String, Object, Exception>()
                .cacheFunction(s -> null)
                .anchor(this)
                .build());
    }

    @Test
    public void testTwoCache() {
        assertThat(firstCache().get(255)).isEqualTo("ff");
        assertThat(secondCache().get(123.456)).isEqualTo(new BigDecimal("123.456"));
    }

    @Test
    public void testBadNullCacheValue() {
        try {
            assertThat(badNullCache().get("whatever"));
            fail("should have thrown BadCacheFunctionException");
        } catch (BadCacheFunctionRuntimeException e) {
            assertThat(e.getMessage()).contains("whatever");
        }
    }

    @Test
    public void testBadNullCacheKey() {
        try {
            assertThat(badNullCache().get(null));
            fail("should have thrown NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("null key (not supported)");
        }
    }

    @Test
    public void testToString() {
        // Make sure implementation has a custom toString(), not Object's default
        assertThat(firstCache().toString()).contains("Cache{");
    }

}
