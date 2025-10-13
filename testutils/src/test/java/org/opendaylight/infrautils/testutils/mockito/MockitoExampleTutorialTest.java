/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.Test;

/**
 * Test to illustrate the basic use of Mockito VS the EXCEPTION_ANSWER.
 *
 * <p>Also useful as example to contrast this approach with the REAL_OR_EXCEPTION
 * approach illustrated in the {@link MockitoTest}.
 *
 * @see MockitoTest
 *
 * @author Michael Vorburger
 */
public class MockitoExampleTutorialTest {

    interface SomeService {

        void foo();

        String bar(String arg);

        // Most methods on real world services have complex input (and output objects), not just int or String
        int foobar(Path file);
    }

    @Test
    public void usingMockitoWithoutStubbing() {
        SomeService service = mock(SomeService.class);
        assertNull(service.bar("hulo"));
    }

    @Test
    public void usingMockitoToStubSimpleCase() {
        SomeService service = mock(SomeService.class);
        when(service.foobar(any())).thenReturn(123);
        assertEquals(123, service.foobar(Path.of("hello.txt")));
    }

    @Test
    public void usingMockitoToStubComplexCase() {
        SomeService service = mock(SomeService.class);
        when(service.foobar(any())).thenAnswer(inv -> inv.getArgument(0).equals(Path.of("hello.txt")) ? 123 : 0);
        assertEquals(0, service.foobar(Path.of("belo.txt")));
    }

    @Test
    public void usingMockitoExceptionException() {
        SomeService service = mock(SomeService.class, MoreAnswers.exception());
        assertThrows(UnstubbedMethodException.class, () -> service.foo());
    }

    @Test
    public void usingMockitoNoExceptionIfStubbed() {
        SomeService service = mock(SomeService.class, MoreAnswers.exception());
        // NOT when(s.foobar(any())).thenReturn(123) BUT must be like this:
        doReturn(123).when(service).foobar(any());
        assertEquals(123, service.foobar(Path.of("hello.txt")));

        assertThrows(UnstubbedMethodException.class, () -> service.foo());
    }

    @Test
    public void usingMockitoToStubComplexCaseAndExceptionIfNotStubbed() {
        SomeService service = mock(SomeService.class, MoreAnswers.exception());
        doAnswer(inv -> inv.getArgument(0).equals(Path.of("hello.txt")) ? 123 : 0).when(service).foobar(any());
        assertEquals(123, service.foobar(Path.of("hello.txt")));
        assertEquals(0, service.foobar(Path.of("belo.txt")));
    }
}
