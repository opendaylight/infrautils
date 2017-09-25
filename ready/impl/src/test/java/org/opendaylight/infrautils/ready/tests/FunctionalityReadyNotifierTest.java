/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.infrautils.ready.internal.FunctionalityReadyNotifierImpl;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyNotifier;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Unit test for {@link FunctionalityReadyNotifier}.
 *
 * @author Michael Vorburger.ch
 */
public class FunctionalityReadyNotifierTest {

    private final BundleContext mockBundleContext = mock(BundleContext.class);
    private final FunctionalityReadyNotifier notifier = new FunctionalityReadyNotifierImpl(mockBundleContext);

    @Test
    public void testRegisterTestReady() throws InvalidSyntaxException {
        when(mockBundleContext.getAllServiceReferences(TestReady.class.getName(), null))
                .thenReturn(new ServiceReference[] {});

        FunctionalityReadyRegistration<?> registration = notifier.register(TestReady.class);
        assertThat(registration).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNull() {
        notifier.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestClassReadyInsteadOfInterface() {
        notifier.register(TestClassReady.class);
    }

    private interface TestReady extends FunctionalityReady { }

    private static class TestClassReady implements FunctionalityReady { }

}
