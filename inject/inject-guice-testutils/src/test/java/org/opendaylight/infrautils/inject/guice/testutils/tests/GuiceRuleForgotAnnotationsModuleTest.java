/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils.tests;

import com.google.inject.AbstractModule;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

/**
 * Tests friendly and more helpful than default error message in case
 * AnnotationsModule was forgotten to be found.
 *
 * @author Michael Vorburger.ch
 */
@SuppressWarnings("checkstyle:IllegalThrows")
public class GuiceRuleForgotAnnotationsModuleTest {

    // This is intentional, with this it fails expectedly; remove to see how
    // public @Rule GuiceRule guice = new GuiceRule(TestModule.class);

    @Test(expected = IllegalStateException.class)
    public void testGuiceWithRule() throws Throwable {
        new GuiceRule(TestModule.class).apply(null, null, null).evaluate();
    }

    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

}
