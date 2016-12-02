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
import org.opendaylight.infrautils.caches.CacheProvider;
import org.opendaylight.infrautils.caches.sample.SampleService;
import org.opendaylight.infrautils.caches.sample.SampleServiceWithCachingImpl;
import org.opendaylight.infrautils.caches.standard.StandardCacheProvider;
import org.opendaylight.infrautils.inject.guice.testutils.AbstractGuiceJsr250Module;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;
import org.ops4j.pax.cdi.api.OsgiService;

/**
 * Test for SampleService.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public class SampleServiceTest {

    public @Rule MethodRule guice = new GuiceRule(SampleServiceTestModule.class);

    @Inject SampleService sampleService;

    @Test
    public void testSayHello() {
        assertThat(sampleService.sayHello("world")).isEqualTo("hello, world");
    }

    public static class SampleServiceTestModule extends AbstractGuiceJsr250Module {
        @Override
        protected void configureBindings() throws Exception {
            bind(SampleService.class).to(SampleServiceWithCachingImpl.class);
            bind(CacheProvider.class).annotatedWith(OsgiService.class).to(StandardCacheProvider.class);
        }
    }
}
