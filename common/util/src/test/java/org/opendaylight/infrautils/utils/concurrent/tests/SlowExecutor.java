/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent.tests;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.Executor;

/**
 * An {@link Executor} that is intentionally slow.
 * Useful for testing concurrency utilities.
 * @author Michael Vorburger.ch
 */
class SlowExecutor implements Executor {

    private final Executor delegate;

    SlowExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(() -> {
            Uninterruptibles.sleepUninterruptibly(100, MILLISECONDS);
            command.run();
        });
    }
}
