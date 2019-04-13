/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Random;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link Executor} that is intentionally slow. Useful for testing concurrency utilities.
 *
 * @author Michael Vorburger.ch
 */
public class SlowExecutor implements Executor {

    private final Random random = new Random();

    private final Executor delegate;

    public SlowExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@Nullable Runnable command) {
        requireNonNull(command);
        delegate.execute(() -> {
            Uninterruptibles.sleepUninterruptibly(random(30, 100), MILLISECONDS);
            command.run();
        });
    }

    private long random(int low, int high) {
        return (long) random.nextInt(high - low) + low;
    }
}
