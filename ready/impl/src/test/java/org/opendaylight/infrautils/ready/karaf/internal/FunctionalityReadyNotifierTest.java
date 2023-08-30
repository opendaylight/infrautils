/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyNotifier;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit test for {@link FunctionalityReadyNotifier}.
 *
 * @author Michael Vorburger.ch
 */
public class FunctionalityReadyNotifierTest {

    private static Object registeredService;

    private abstract static class AbstractBundleContextImpl implements BundleContext {
        @Override
        @SuppressWarnings("unchecked")
        public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
            registeredService = service;
            return mock(ServiceRegistration.class);
        }
    }

    private final BundleContext mockBundleContext = mock(AbstractBundleContextImpl.class);
    private final FunctionalityReadyNotifierImpl notifier = new FunctionalityReadyNotifierImpl();

    @Before
    public void before() {
        notifier.activate(mockBundleContext);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRegisterTestReady() throws InvalidSyntaxException {
        when(mockBundleContext.getAllServiceReferences(TestReady.class.getName(), null))
                .thenReturn(new ServiceReference[] {});
        when(mockBundleContext.registerService((Class<TestReady>) any(), any(TestReady.class), any()))
                .thenCallRealMethod();

        FunctionalityReadyRegistration<?> registration = notifier.register(TestReady.class);
        assertNotNull(registration);
        assertNotNull(registeredService);
        assertNotNull(registeredService.toString());
        assertThat(registeredService.hashCode(), greaterThan(0));
        assertFalse(registeredService.equals(null));
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNull() {
        notifier.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestClassReadyInsteadOfInterface() {
        notifier.register(TestClassReady.class);
    }

    private interface TestReady extends FunctionalityReady {

    }

    private static final class TestClassReady implements FunctionalityReady {

    }
}
