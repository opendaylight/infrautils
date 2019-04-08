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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.caches.BadCacheFunctionRuntimeException;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.Cache;
import org.opendaylight.infrautils.caches.CacheConfigBuilder;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.CachePolicyBuilder;
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.CheckedCache;
import org.opendaylight.infrautils.caches.CheckedCacheConfigBuilder;
import org.opendaylight.infrautils.testutils.LogRule;

/**
 * Base Unit Test for CacheProvider.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public abstract class AbstractCacheProviderTest {

    @Rule public LogRule logRule = new LogRule();

    private Cache<Integer, String> firstUncheckedCache;
    private Cache<Double, BigDecimal> secondUncheckedCache;

    @Inject private CacheManagers cachesMonitor;
    @Inject private CacheProvider cacheProvider;

    @PostConstruct
    void init() {
        firstUncheckedCache = cacheProvider.newCache(
                new CacheConfigBuilder<Integer, String>()
                    .cacheFunction(Integer::toHexString)
                    .anchor(this)
                    .build());

        secondUncheckedCache = cacheProvider.newCache(
            new CacheConfigBuilder<Double, BigDecimal>()
                .cacheFunction(BigDecimal::valueOf)
                .anchor(this)
                .build());
    }

    @Test
    public void testTwoCaches() {
        assertThat(firstUncheckedCache.get(255)).isEqualTo("ff");
        assertThat(secondUncheckedCache.get(123.456)).isEqualTo(new BigDecimal("123.456"));
    }

    @Test
    public void testGetAll() {
        assertThat(firstUncheckedCache.get(255, 10).values()).containsExactly("ff", "a");
        assertThat(firstUncheckedCache.get(255, 10)).containsExactly(255, "ff", 10, "a");
    }

    @Test
    public void testPut() throws Exception {
        AtomicBoolean cacheFunctionUsed = new AtomicBoolean(false);
        Cache<Integer, String> cache = cacheProvider.newCache(
            new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> {
                    cacheFunctionUsed.set(true);
                    return Integer.toHexString(i);
                })
                .anchor(this)
                .build());
        cache.put(10, Integer.toHexString(10));
        assertThat(cache.get(10)).isEqualTo("a");
        assertThat(cacheFunctionUsed.get()).isEqualTo(false);

        assertThat(cache.get(255)).isEqualTo("ff");
        assertThat(cacheFunctionUsed.get()).isEqualTo(true);

        cache.close();
    }

    @Test
    public void testCacheMonitorRegistration() {
        assertThat(cachesMonitor.getAllCacheManagers()).hasSize(2);
        CacheManager firstCachesManager = cachesMonitor.getAllCacheManagers().iterator().next();

        BaseCacheConfig config = firstCachesManager.getConfig();
        assertThat(config.id()).isEqualTo(getClass().getName());
    }

    @Test
    public void testCacheMonitorPolicyAndStat() {
        CacheManager firstCachesManager = cachesMonitor.getAllCacheManagers().iterator().next();
        assertThat(firstUncheckedCache.get(254)).isEqualTo("fe");

        assertThat(firstCachesManager.getPolicy().statsEnabled()).isFalse();
        assertThat(firstCachesManager.getStats().missCount()).isEqualTo(0);

        firstCachesManager.setPolicy(new CachePolicyBuilder()
                .from(firstCachesManager.getPolicy())
                .statsEnabled(true)
                .build());

        assertThat(firstCachesManager.getStats().estimatedCurrentEntries()).isEqualTo(0);
        assertThat(firstUncheckedCache.get(255)).isEqualTo("ff");
        assertThat(firstCachesManager.getStats().estimatedCurrentEntries()).isEqualTo(1);

        assertThat(firstCachesManager.getStats().missCount()).isEqualTo(1);
        assertThat(firstCachesManager.getStats().extensions().size()).isAtLeast(6);

        firstCachesManager.evictAll();
        assertThat(firstCachesManager.getStats().estimatedCurrentEntries()).isEqualTo(0);
    }

    @Test
    public void testThrowingUncheckedCache() {
        try {
            cacheProvider.newCache(
                new CacheConfigBuilder<String, Long>()
                    .cacheFunction(d -> {
                        throw new IllegalStateException("boum");
                    })
                    .anchor(this)
                    .build()).get("something");
            fail("should have thrown CacheRuntimeException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("boum");
        }
    }

    private CheckedCache<File, String, IOException> checkedCache() {
        return cacheProvider.newCheckedCache(
            new CheckedCacheConfigBuilder<File, String, IOException>()
                .cacheFunction(f -> "world" /* real world impl. could e.g. read file contents */)
                .anchor(this)
                .build());
    }

    private CheckedCache<File, String, IOException> checkedThrowingCache() {
        return cacheProvider.newCheckedCache(
            new CheckedCacheConfigBuilder<File, String, IOException>()
                .cacheFunction(f -> {
                    throw new IOException("boum");
                })
                .anchor(this)
                .build());
    }

    @Test
    public void testCheckedCache() throws IOException { // NB throws IOException, like cache's function
        assertThat(checkedCache().get(new File("hello.txt"))).isEqualTo("world");
    }

    @Test
    public void testCheckedCacheGetAll() throws IOException { // NB throws IOException, like cache's function
        assertThat(checkedCache().get(new File("hello.txt"), new File("hello2.txt")).values())
            .containsExactly("world", "world");
    }

    @Test
    public void testPutCheckedCache() throws Exception {
        // This is technically a wrong usage of the API, as per the JavaDoc,
        // contrary to the testPut() for the unchecked cache which does it right,
        // but for a quick unit test here, it will do anyway:
        CheckedCache<File, String, IOException> checkedCache = checkedCache();
        checkedCache.put(new File("hello.txt"), "badValueInconsistentValue");
        assertThat(checkedCache.get(new File("hello.txt"))).isEqualTo("badValueInconsistentValue");
        checkedCache.close();
    }

    @Test(expected = IOException.class)
    public void testThrowingCheckedCache() throws IOException {
        assertThat(checkedThrowingCache().get(new File("hello.txt"))).isEqualTo("world");
    }

    @Test(expected = NullPointerException.class)
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void testPutNullKey() {
        firstUncheckedCache.put(null, "...");
    }

    @Test(expected = NullPointerException.class)
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void testPutNullKeyIntoCheckedCache() {
        checkedCache().put(null, "...");
    }

    @Test(expected = NullPointerException.class)
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void testPutNullValue() {
        firstUncheckedCache.put(123, null);
    }

    @Test(expected = NullPointerException.class)
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    public void testPutNullValueIntoCheckedCache() {
        checkedCache().put(new File("."), null);
    }

    private Cache<String, Object> badNullCache() {
        return cacheProvider.newCache(
            new CacheConfigBuilder<String, Object>()
                .cacheFunction(s -> null)
                .anchor(this)
                .build());
    }

    @Test
    public void testBadNullCacheValue() {
        try {
            badNullCache().get("whatever");
            fail("should have thrown BadCacheFunctionException");
        } catch (BadCacheFunctionRuntimeException e) {
            assertThat(e.getMessage()).contains("whatever");
        }
    }

    private CheckedCache<String, Object, IOException> badNullCheckedCache() {
        return cacheProvider.newCheckedCache(
            new CheckedCacheConfigBuilder<String, Object, IOException>()
                .cacheFunction(s -> null)
                .anchor(this)
                .build());
    }

    @Test
    public void testBadNullCheckedCacheValue() throws IOException {
        try {
            badNullCheckedCache().get("whatever");
            fail("should have thrown BadCacheFunctionException");
        } catch (BadCacheFunctionRuntimeException e) {
            assertThat(e.getMessage()).contains("whatever");
        }
    }

    @Test
    public void testBadNullCacheKey() {
        try {
            badNullCache().get((String) null);
            fail("should have thrown NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("null key (not supported)");
        }
    }

    @Test
    public void testToString() {
        // Make sure implementation has a custom toString(), not Object's default
        assertThat(firstUncheckedCache.toString()).contains("Cache{");
    }

}
