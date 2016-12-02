/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.tests;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.opendaylight.infrautils.caches.CacheConfig;
import org.opendaylight.infrautils.caches.CacheConfigBuilder;

/**
 * Unit Test for Cache's Config.
 *
 * @author Michael Vorburger.ch
 */
public class CacheConfigTest {

    @Test
    public void testBuildSimplestConfig() {
        CacheConfig<Integer, String> config = new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                // id() & description() are optional!
                .build();

        // Test expected defaults
        assertThat(config.id()).isEqualTo(getClass().getName().toLowerCase());
        assertThat(config.description()).isEqualTo("");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingRequiredConfigurationProperties() {
        new CacheConfigBuilder<Integer, String>().build();
    }

    @Test
    public void testBuildConfigWithID() {
        new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .id("first.cache")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildConfigWithIDWithDashInsteadOfDot() {
        new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .id("first-cache")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildConfigWithTooLongNameWithSpaces() {
        new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .id("this really is a description rather than a name")
                .build();
    }

    @Test
    public void testBuildConfigWithNameAndDescription() {
        new CacheConfigBuilder<Integer, String>()
                .cacheFunction(i -> Integer.toHexString(i))
                .anchor(this)
                .id("another.cache")
                .description("Great cache; really great, it's HUGE. #FakeNews")
                .build();
    }

    @Test
    public void testToString() {
        CacheConfig<Integer, String> config = new CacheConfigBuilder<Integer, String>()
            .cacheFunction(i -> Integer.toHexString(i))
            .anchor(this)
            .build();
        // Make sure implementation has a custom toString(), not Object's default
        assertThat(config.toString()).startsWith("CacheConfig{");
    }

}
