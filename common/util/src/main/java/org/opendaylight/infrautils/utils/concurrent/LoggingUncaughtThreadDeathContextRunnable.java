/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.base.Preconditions;
import java.util.function.Supplier;
import org.slf4j.Logger;

/**
 * Runnable with final {@link #run()} method that catches any unexpected checked
 * exceptions ({@link RuntimeException} &amp; {@link Error}) and logs them with
 * some kind of context which allows to better identify the root cause.
 *
 * @author Michael Vorburger.ch
 */
public abstract class LoggingUncaughtThreadDeathContextRunnable implements Runnable {

    private final Logger logger;
    private final Supplier<String> debugLogContextSupplier;

    /**
     * Constructor.
     *
     * @param logger the Logger of the class which created this Runnable
     * @param debugLogContextSupplier supplies useful context included in error log in case of caught checked exception
     */
    protected LoggingUncaughtThreadDeathContextRunnable(Logger logger, Supplier<String> debugLogContextSupplier) {
        this.logger = Preconditions.checkNotNull(logger, "logger");
        this.debugLogContextSupplier = debugLogContextSupplier;
    }

    /**
     * Final run() method; subclasses implement {@link #runWithUncheckedExceptionLogging()} instead of this.
     */
    @Override
    @SuppressWarnings("checkstyle:IllegalCatch") // OK because we rethrow (we just want to also log context)
    public final void run() {
        try {
            runWithUncheckedExceptionLogging();
        } catch (Throwable e) {
            logger.error("Runnnable likely about to terminate thread due to uncaught exception; "
                    + "but here is useful debugging context: {}", debugLogContextSupplier.get(), e);
            // Must rethrow, because the Thread must die...
            // (assuming this Runnable was submit into an ExecutorService or Thread constructor)
            throw e;
        }
    }

    /**
     * Run method which you implement instead of the original {@link #run()}.
     */
    protected abstract void runWithUncheckedExceptionLogging();

}
