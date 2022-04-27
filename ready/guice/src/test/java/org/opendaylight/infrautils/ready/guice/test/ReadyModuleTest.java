/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.guice.test;

import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.testutils.AnnotationsModule;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.guice.ReadyModule;

/**
 * Unit test for {@link ReadyModule}.
 *
 * @author Michael Vorburger.ch
 */
public class ReadyModuleTest {
    public static class TestBean {
        private final SystemReadyMonitor systemReadyMonitor;
        boolean invokedReadyCallbackRegisteredInConstructor = false;
        boolean invokedReadyCallbackRegisteredInPostConstruct = false;

        @Inject public TestBean(final SystemReadyMonitor systemReadyMonitor) {
            this.systemReadyMonitor = systemReadyMonitor;
            systemReadyMonitor.registerListener(() -> {
                invokedReadyCallbackRegisteredInConstructor = true;
            });
        }

        @PostConstruct public void init() {
            systemReadyMonitor.registerListener(() -> {
                invokedReadyCallbackRegisteredInPostConstruct = true;
            });
        }
    }

    @Rule public GuiceRule guice = new GuiceRule(ReadyModule.class, AnnotationsModule.class);

    @Inject TestBean testBean;

    @Test
    public void testReadyListener() {
        assertTrue(testBean.invokedReadyCallbackRegisteredInConstructor);
        assertTrue(testBean.invokedReadyCallbackRegisteredInPostConstruct);
    }
}
