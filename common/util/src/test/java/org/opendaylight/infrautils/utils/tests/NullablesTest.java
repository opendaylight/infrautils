/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.tests;

import java.io.IOException;
import org.junit.Test;
import org.opendaylight.infrautils.utils.Nullables;

public class NullablesTest {

    @Test
    public void testCheckedConsumerApplyIfNonNullNoException() {
        Nullables.applyIfNonNull(null, v -> {
            // ...
        });
    }

    @Test
    public void testCheckedConsumerApplyIfNonNullRuntimeException() {
        Nullables.ifNonNull(null, v -> {
            throw new IllegalStateException("kaboum");
        });
    }

    @Test
    public void testCheckedConsumerApplyIfNonNullCheckedException() throws IOException {
        Nullables.applyIfNonNull(null, v -> {
            throwing();
        });
    }

    private void throwing() throws IOException {
        // ...
    }

}
