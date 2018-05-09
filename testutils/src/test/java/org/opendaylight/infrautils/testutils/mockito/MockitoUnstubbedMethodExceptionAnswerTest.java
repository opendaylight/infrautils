/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;

public class MockitoUnstubbedMethodExceptionAnswerTest {

    @Test
    public void testAnswering() throws IOException {
        Closeable mock = Mockito.mock(Closeable.class, MoreAnswers.exception());
        try {
            mock.close();
            fail();
        } catch (UnstubbedMethodException e) {
            assertThat(e.getMessage()).isEqualTo("close() is not stubbed in mock of java.io.Closeable");
        }

    }

}
