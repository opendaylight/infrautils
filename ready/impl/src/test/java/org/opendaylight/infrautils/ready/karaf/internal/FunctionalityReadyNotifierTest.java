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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyNotifier;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit test for {@link FunctionalityReadyNotifier}.
 *
 * @author Michael Vorburger.ch
 */
@ExtendWith(MockitoExtension.class)
class FunctionalityReadyNotifierTest {
    @Mock
    private BundleContext mockBundleContext;
    @Mock
    private ServiceRegistration<?> serviceRegistration;

    private FunctionalityReadyNotifierImpl notifier;
    private Object registeredService;

    @BeforeEach
    void beforeEach() {
        notifier = new FunctionalityReadyNotifierImpl(mockBundleContext);
    }

    @Test
    void testRegisterTestReady() throws Exception {
        doAnswer(invocation -> {
            registeredService = invocation.getArgument(1);
            return mock(ServiceRegistration.class);
        }).when(mockBundleContext).registerService(any(Class.class), any(Object.class), any());

        doReturn(new ServiceReference[] {}).when(mockBundleContext)
            .getAllServiceReferences(TestReady.class.getName(), null);

        FunctionalityReadyRegistration<?> registration = notifier.register(TestReady.class);
        assertNotNull(registration);
        assertNotNull(registeredService);
        assertNotNull(registeredService.toString());
        assertThat(registeredService.hashCode(), greaterThan(0));
        assertFalse(registeredService.equals(null));
    }

    @Test
    void testRegisterNull() {
        assertThrows(NullPointerException.class, () -> notifier.register(null));
    }

    @Test
    void testTestClassReadyInsteadOfInterface() {
        assertThrows(IllegalArgumentException.class, () -> notifier.register(TestClassReady.class));
    }

    private interface TestReady extends FunctionalityReady {

    }

    private static final class TestClassReady implements FunctionalityReady {

    }
}
