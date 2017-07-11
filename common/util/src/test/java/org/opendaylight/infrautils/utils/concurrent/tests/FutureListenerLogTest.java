/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent.tests;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.utils.concurrent.MoreFutures;
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

    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();

    @Test
    public void testFailingListenableFuture() {
        logCaptureRule.expectError("Future (eventually) failed: duh");
        ListenableFuture<Void> failedFuture = Futures.immediateFailedFuture(new Exception("some problem"));
        MoreFutures.logFailure(failedFuture, "duh");
    }

    @Test
    public void testPassingListenableFuture() {
        ListenableFuture<String> stringFuture = Futures.immediateFuture("hello");
        MoreFutures.logFailure(stringFuture, "huh?!");
    }

}
