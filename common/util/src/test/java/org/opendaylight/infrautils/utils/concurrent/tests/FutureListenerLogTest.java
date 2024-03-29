/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.opendaylight.infrautils.utils.concurrent.LoggingFutures.addErrorLogging;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test, and example, for how to correctly log failures from async calls
 * returning Future.
 *
 * @author Michael Vorburger.ch
 */
public class FutureListenerLogTest {

    private static final Logger LOG = LoggerFactory.getLogger(FutureListenerLogTest.class);

    @Rule public LogCaptureRule logCaptureRule = new LogCaptureRule();

    @Test
    public void testFailingListenableFuture() {
        logCaptureRule.expectError("Future (eventually) failed: duh");
        ListenableFuture<Void> failedFuture = Futures.immediateFailedFuture(new Exception("some problem"));
        assertSame(failedFuture, addErrorLogging(failedFuture, LOG, "duh"));
    }

    @Test
    public void testPassingListenableFuture() {
        ListenableFuture<String> stringFuture = Futures.immediateFuture("hello");
        assertSame(stringFuture, addErrorLogging(stringFuture, LOG, "huh?!"));
    }

    @Test
    public void testFailingListenableFutureWithOneMessageFormatArgument() {
        logCaptureRule.expectError("Future (eventually) failed: duh dah");
        ListenableFuture<Integer> failedFuture = Futures.immediateFailedFuture(new Exception("some problem"));
        assertSame(failedFuture, addErrorLogging(failedFuture, LOG, "duh {}", new ObjectWithToString("dah")));
    }

    @Test
    public void testFailingListenableFutureWithTwoMessageFormatArguments() {
        logCaptureRule.expectError("Future (eventually) failed: duh bah doo");
        ListenableFuture<Integer> failedFuture = Futures.immediateFailedFuture(new Exception("some problem"));
        assertSame(failedFuture, addErrorLogging(failedFuture, LOG, "duh {} {}",
                new ObjectWithToString("bah"), new ObjectWithToString("doo")));
    }

    @Test
    public void testFailingListenableFutureWithThreeMessageFormatArguments() {
        logCaptureRule.expectError("Future (eventually) failed: ho/he/do");
        ListenableFuture<Integer> failedFuture = Futures.immediateFailedFuture(new Exception("some problem"));
        assertSame(failedFuture, addErrorLogging(failedFuture, LOG, "{}/{}/{}",
                new ObjectWithToString("ho"),
                new ObjectWithToString("he"),
                new ObjectWithToString("do")));
        assertEquals("some problem", logCaptureRule.getLastErrorThrowable().getMessage());
    }

    private static final class ObjectWithToString {
        private final String string;

        ObjectWithToString(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
