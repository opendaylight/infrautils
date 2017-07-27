/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link Origins}.
 *
 * @author Michael Vorburger.ch
 */
public class OriginsTest {

    @Test
    public void testOriginsNextIdAPI() {
        String firstID = Origins.nextOriginID();
        String secondID = Origins.nextOriginID();
        assertThat(firstID).isNotNull();
        assertThat(firstID).isNotEqualTo(secondID);
    }

    @Test
    public void testOriginsNextIdImplementation() {
        Origins.resetOriginID_used_only_for_testing(0);
        assertThat(Origins.nextOriginID()).isEqualTo("0000000000000");
        assertThat(Origins.nextOriginID()).isEqualTo("0000000000001");
        assertThat(Origins.nextOriginID()).isEqualTo("0000000000002");
        assertThat(Origins.nextOriginID()).isEqualTo("0000000000003");

        Origins.resetOriginID_used_only_for_testing(0xfe2abf3f4fcfdf8fL);
        assertThat(Origins.nextOriginID()).isEqualTo("FSALV7T7SVNSF");

        Origins.resetOriginID_used_only_for_testing(0xffffffffffffffffL - 1);
        assertThat(Origins.nextOriginID()).isEqualTo("FVVVVVVVVVVVU");
        assertThat(Origins.nextOriginID()).isEqualTo("FVVVVVVVVVVVV");
        assertThat(Origins.nextOriginID()).isEqualTo("0000000000000");
    }

    @Test(expected = IllegalStateException.class)
    public void testFailingOriginsCurrentIdAPI() {
        Origins.currentOriginID();
    }

    @Test
    @Ignore // see @Ignore in MDCTest (same reason; will un-ignore later)
    public void testOriginsCurrentIdAPI() {
        String oid = Origins.nextOriginID();
        MDCs.putRunRemove(Origins.OID_MDC_KEY, oid, () -> {
            assertThat(Origins.currentOriginID()).isEqualTo(oid);
        });
    }

}
