/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * Unit test illustrating usage of {@link MDCs}.
 *
 * @author Michael Vorburger.ch
 */
public class MDCTest {

    @Test
    public void testPutDuplicateKeyMDC() {
        // This works (at least in some MDCAdapter implementations)
        MDC.put("yo", "ya");
        MDC.put("yo", "ha");
    }

    @Ignore // This cannot work yet with the slf4j-simple enforced by odlparent
    // TODO Either this needs to be made a PAX exam test, or odlparent should have log4j instead of slf4j-simple
    @Test(expected = IllegalArgumentException.class)
    public void testPutDuplicateKeyMDCs() {
        MDCs.put("yo", "ha");
        // Whereas this will throw an IllegalArgumentException
        MDCs.put("yo", "ha");
    }

    @Test
    public void testPutNullValueMDC() {
        // This works
        MDC.put("yo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullValueMDCs() {
        // Whereas this will throw an IllegalArgumentException
        MDCs.put("yo", null);
    }

    @Test
    @Ignore // as above
    public void testPutRunRemove() {
        AtomicBoolean done = new AtomicBoolean(false);
        MDCs.putRunRemove("yo", "ya", () -> {
            assertThat(MDC.get("yo")).isEqualTo("ya");
            done.set(true);
        });
        assertThat(done.get()).isTrue();
        assertThat(MDC.get("yo")).isNull();
    }
}
