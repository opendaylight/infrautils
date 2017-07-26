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
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("AAAAAAAAAAAAA===");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("AAAAAAAAAAAAC===");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("AAAAAAAAAAAAE===");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("AAAAAAAAAAAAG===");

        ExecutionOrigin.resetOriginID_used_only_for_testing(0xfe2abf3f4fcfdf8fL);
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("7YVL6P2PZ7PY6===");

        ExecutionOrigin.resetOriginID_used_only_for_testing(0xffffffffffffffffL - 1);
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("7777777777774===");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("7777777777776===");
        assertThat(ExecutionOrigin.next().toString()).isEqualTo("AAAAAAAAAAAAA===");
    }


    @Test(expected = IllegalStateException.class)
    public void testFailingOriginsCurrentIdAPI() {
        ExecutionOrigin.currentID();
    }

    @Test
    @Ignore // see @Ignore in MDCTest (same reason; will un-ignore later)
    public void testOriginsCurrentIdAPI() {
        ExecutionOrigin nextExecutionOrigin = ExecutionOrigin.next();
        MDCs.putRunRemove(nextExecutionOrigin.mdcKeyString(), nextExecutionOrigin.mdcValueString(), () -> {
            assertThat(ExecutionOrigin.currentID()).isEqualTo(nextExecutionOrigin.mdcValueString());
        });
    }
}
