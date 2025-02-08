/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.odlparent.bundlestest.lib;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.opendaylight.odlparent.bundles.diag.DiagProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to verify bundle diagnostic state.
 *
 * @author Michael Vorburger.ch, based on guidance from Christian Schneider
 */
public class TestBundleDiag {
    private static final Logger LOG = LoggerFactory.getLogger(TestBundleDiag.class);

    private final DiagProvider diagProvider;

    public TestBundleDiag(DiagProvider diagProvider) {
        this.diagProvider = requireNonNull(diagProvider);
    }

    /**
     * Does something similar to Karaf's "diag" CLI command, and throws a {@link SystemStateFailureException} if
     * anything including bundle wiring is not OK.
     *
     * <p>The implementation is based on Karaf's BundleService, and not the BundleStateService, because each Karaf
     * supported DI system (such as Blueprint and Declarative Services, see String constants in BundleStateService),
     * will have a separate BundleStateService.  The BundleService however will contain the combined status of all
     * BundleStateServices.
     *
     * @param timeout maximum time to wait for bundles to settle
     * @param timeoutUnit time unit of timeout
     * @throws SystemStateFailureException if all bundles do not settle within the timeout period
     */
    public void checkBundleDiagInfos(long timeout, TimeUnit timeoutUnit)
            throws SystemStateFailureException {
        checkBundleDiagInfos(timeout, timeoutUnit, (timeInfo, bundleDiagInfos) ->
            LOG.info("checkBundleDiagInfos: Elapsed time {}s, remaining time {}s, {}",
                timeInfo.elapsedTimeInMS() / 1000, timeInfo.remainingTimeInMS() / 1000,
                bundleDiagInfos.getFullDiagnosticText()));
    }

    public void checkBundleDiagInfos(long timeout, TimeUnit timeoutUnit,
            BiConsumer<TimeInfo, BundleDiagInfos> awaitingListener) throws SystemStateFailureException {
        LOG.info("checkBundleDiagInfos() started...");

        var timeoutNanos = timeoutUnit.toNanos(timeout);
        var started = System.nanoTime();

        while (true) {
            var elapsedNanos = System.nanoTime() - started;
            var remainingNanos = timeoutNanos - elapsedNanos;
            var diag = diagProvider.currentDiag();
            var bundleInfos = BundleDiagInfosImpl.ofDiag(diag);

            var systemState = bundleInfos.getSystemState();
            switch (systemState) {
                case Active -> {
                    // Inform the developer of the green SystemState.Active
                    awaitingListener.accept(new TimeInfo(TimeUnit.NANOSECONDS.toMillis(elapsedNanos),
                        TimeUnit.NANOSECONDS.toMillis(remainingNanos)), bundleInfos);
                    LOG.info("diag successful; system state active ({})", bundleInfos.getFullDiagnosticText());
                    return;
                }
                case Failure, Stopping -> {
                    LOG.error("""
                        diag failure; BundleService reports bundle(s) which failed or are already stopping (details in \
                        following INFO and ERROR log messages...)""");
                    diag.logState(LOG);
                    throw new SystemStateFailureException("diag failed; some bundles failed to start", bundleInfos);
                }

                // FIXME: a.k.a Booting, but the combination of checkstyle and error-prone requires us to use this ugly
                //        construct. Reasons:
                //        - we need either a 'default' or 'case null' for checkstyle
                //        - using a catch-all 'default' is hated by error-prone
                //        - using a 'case null' without 'default' results in
                //          https://github.com/google/error-prone/issues/4721
                //        So when we have fixed error-prone, we need to go back to 'case Booting' and 'case null'.
                default -> {
                    if (remainingNanos <= 0) {
                        // This typically happens due to bundles still in BundleState GracePeriod or Waiting
                        LOG.error("""
                            diag failure; BundleService reports bundle(s) which are still not active (details in \
                            following INFO and ERROR log messages...)""");
                        diag.logState(LOG);
                        throw new SystemStateFailureException("diag timeout; some bundles are still not active:",
                            bundleInfos);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new SystemStateFailureException("Interrupted waiting for a retry", bundleInfos, e);
                    }
                }
            }
        }
    }
}
