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
 * Unit test for {@link ExecutionOrigin}.
 *
 * @author Michael Vorburger.ch
 */
public class ExecutionOriginTest {

    @Test
    public void testOriginsNextAPI() {
        ExecutionOrigin firstID = ExecutionOrigin.next();
        ExecutionOrigin secondID = ExecutionOrigin.next();
        assertThat(firstID).isNotNull();
        assertThat(firstID).isNotEqualTo(secondID);
    }

    @Test
    public void testOriginsNextIdAPI() {
        String firstID = ExecutionOrigin.next().toString();
        String secondID = ExecutionOrigin.next().toString();
        assertThat(firstID).isNotNull();
        assertThat(firstID).isNotEqualTo(secondID);
    }

    @Test
    public void testOriginsNextIdImplementation() {
        ExecutionOrigin.resetOriginID_used_only_for_testing(0);
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("0000000000000");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("0000000000001");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("0000000000002");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("0000000000003");

//        ExecutionOrigin.resetOriginID_used_only_for_testing(0xfe2abf3f4fcfdf8fL);
//        assertThat(ExecutionOrigin.next().toString()).isEqualTo("FSALV7T7SVNSF");

        ExecutionOrigin.resetOriginID_used_only_for_testing(0xffffffffffffffffL - 1);
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("FVVVVVVVVVVVU");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("FVVVVVVVVVVVV");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("0000000000000");
    }


    @Test(expected = IllegalStateException.class)
    public void testFailingOriginsCurrentIdAPI() {
        ExecutionOrigin.currentID();
    }

    @Test
    @Ignore // see @Ignore in MDCTest (same reason; will un-ignore later)
    public void testOriginsCurrentIdAPI() {
        String oid = ExecutionOrigin.next().toString();
        MDCs.putRunRemove(ExecutionOrigin.MDC_KEY, oid, () -> {
            assertThat(ExecutionOrigin.currentID()).isEqualTo(oid);
        });
    }
}
