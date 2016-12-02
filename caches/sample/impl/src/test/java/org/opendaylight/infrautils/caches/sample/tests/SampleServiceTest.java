/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.sample.tests;

import static com.google.common.truth.Truth.assertThat;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.opendaylight.infrautils.caches.sample.SampleService;
import org.opendaylight.infrautils.caches.sample.SampleServiceWithCachingImpl;
import org.opendaylight.infrautils.caches.testutils.CacheModule;
import org.opendaylight.infrautils.inject.guice.testutils.AbstractGuiceJsr250Module;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

/**
 * Test for SampleService.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public class SampleServiceTest {

    public @Rule MethodRule guice = new GuiceRule(
            new SampleServiceTestModule(), new CacheModule(/* NoopCacheProvider.class */));

    @Inject SampleService sampleService;

    @Test
    public void testSayHello() {
        // TODO use com.google.common.base.Stopwatch
        final long t1 = System.currentTimeMillis();
        assertThat(sampleService.sayHello("world")).isEqualTo("hello, world");
        final long d1 = System.currentTimeMillis() - t1;

        final long t2 = System.currentTimeMillis();
        assertThat(sampleService.sayHello("world")).isEqualTo("hello, world");
        final long d2 = System.currentTimeMillis() - t2;

        // The first call has to be A LOT faster the second, if there is a real cache
        assertThat(d1 > d2 * 10).isTrue();
    }

    public static class SampleServiceTestModule extends AbstractGuiceJsr250Module {
        @Override
        protected void configureBindings() throws Exception {
            bind(SampleService.class).to(SampleServiceWithCachingImpl.class);
        }
    }
}
