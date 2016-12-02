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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfigBuilder;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfigBuilder;
import org.opendaylight.infrautils.caches.NonDistributedNonTransactionalCacheProvider;

/**
 * Base Unit Test for CacheProvider.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCacheProviderTest {

    // public @Rule LogRule logRule = new LogRule();

    protected abstract NonDistributedNonTransactionalCacheProvider getCacheProviderSingleton();

    private Cache<Integer, String> firstUncheckedCache() {
        return getCacheProviderSingleton().newCache(
            new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .build());
    }

    private Cache<Double, BigDecimal> secondUncheckedCache() {
        return getCacheProviderSingleton().newCache(
            new CacheConfigBuilder<Double, BigDecimal>()
                .cacheFunction(d -> BigDecimal.valueOf(d))
                .anchor(this)
                .build());
    }

    @Test
    public void testTwoCache() {
        assertThat(firstUncheckedCache().get(255)).isEqualTo("ff");
        assertThat(secondUncheckedCache().get(123.456)).isEqualTo(new BigDecimal("123.456"));
    }

    private Cache<String, Long> uncheckedThrowingCache() {
        return getCacheProviderSingleton().newCache(
            new CacheConfigBuilder<String, Long>()
                .cacheFunction(d -> {
                    throw new IllegalStateException("boum"); })
                .anchor(this)
                .build());
    }

    @Test
    public void testThrowingUncheckedCache() {
        try {
            uncheckedThrowingCache().get("something");
            fail("should have thrown CacheRuntimeException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("boum");
        }
    }

    private CheckedCache<File, String, IOException> checkedCache() {
        return getCacheProviderSingleton().newCheckedCache(
            new CheckedCacheConfigBuilder<File, String, IOException>()
                .cacheFunction(f -> "world" /* real world impl. could e.g. read file contents */)
                .anchor(this)
                .build());
    }

    private CheckedCache<File, String, IOException> checkedThrowingCache() {
        return getCacheProviderSingleton().newCheckedCache(
            new CheckedCacheConfigBuilder<File, String, IOException>()
                .cacheFunction(f -> {
                    throw new IOException("boum"); })
                .anchor(this)
                .build());
    }

    @Test
    public void testCheckedCache() throws IOException { // NB throws IOException, like cache's function
        assertThat(checkedCache().get(new File("hello.txt"))).isEqualTo("world");
    }

    @Test(expected = IOException.class)
    public void testThrowingCheckedCache() throws IOException {
        assertThat(checkedThrowingCache().get(new File("hello.txt"))).isEqualTo("world");
    }

    private Cache<String, Object> badNullCache() {
        return getCacheProviderSingleton().newCache(
            new CacheConfigBuilder<String, Object>()
                .cacheFunction(s -> null)
                .anchor(this)
                .build());
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

    private CheckedCache<String, Object, IOException> badNullCheckedCache() {
        return getCacheProviderSingleton().newCheckedCache(
            new CheckedCacheConfigBuilder<String, Object, IOException>()
                .cacheFunction(s -> null)
                .anchor(this)
                .build());
    }

    @Test
    public void testBadNullCheckedCacheValue() throws IOException {
        try {
            assertThat(badNullCheckedCache().get("whatever"));
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
        assertThat(firstUncheckedCache().toString()).contains("Cache{");
    }

}
