/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils.tests;

import static com.google.common.truth.Truth.assertThat;

import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.testutils.AbstractGuiceJsr250Module;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

/**
 * Example Guice Test using the {@link GuiceRule} & {@link AbstractGuiceJsr250Module}.
 *
 * @author Michael Vorburger
 */
public class ExampleGuiceRuleTest {

    public @Rule GuiceRule guice = new GuiceRule(TestModule.class);

    @Inject SomeInterfaceWithPostConstruct someService;

    @Test public void testGuiceWithRule() {
        assertThat(someService.isInit()).named("isInit()").isTrue();
    }

    public static class TestModule extends AbstractGuiceJsr250Module {
        @Override
        protected void configureBindings() {
            bind(SomeInterfaceWithPostConstruct.class).to(SomeClassWithPostConstruct.class);
        }
    }
}
