/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils.tests;

import static com.google.common.truth.Truth.assertThat;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import org.junit.Test;

/**
 * Example Guice Test without using Rule, just for illustration.
 *
 * @see ExampleGuiceRuleTest
 *
 * @author Michael Vorburger
 */
public class ExamplePureGuiceTest {

    @Test
    public void testPostConstruct() {
        Injector injector = Guice.createInjector(new TestModule());
        SomeClassWithPostConstruct someClass = injector.getInstance(SomeClassWithPostConstruct.class);
        assertThat(someClass.isInit).named("isInit").isTrue();
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new CloseableModule());
            install(new Jsr250Module());
            bind(SomeClassWithPostConstruct.class).asEagerSingleton();
        }
    }
}
