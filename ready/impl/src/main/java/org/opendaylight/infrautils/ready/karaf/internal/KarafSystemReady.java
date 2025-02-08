/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.infrautils.ready.SystemState.FAILURE;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.spi.DelegatingSystemReadyMonitorMXBean;
import org.opendaylight.infrautils.ready.spi.SimpleSystemReadyMonitor;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.opendaylight.odlparent.bundles.diag.DiagProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SystemReadyMonitor} implementation for an OSGi Karaf environment. This
 * relies on odlparent.bundlestest.lib, which internally uses Karaf specific API
 * to get Blueprint status, in addition to basic OSGi bundle status.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
@Component(immediate = true, service = SystemReadyMonitor.class, configurationPid = "org.opendaylight.infrautils.ready")
@Designate(ocd = KarafSystemReady.Config.class)
public final class KarafSystemReady extends SimpleSystemReadyMonitor {
    @ObjectClassDefinition()
    public @interface Config {
        @AttributeDefinition(name = "system-ready-timeout-seconds")
        int systemReadyTimeout() default 300;
    }

    private static final Logger LOG = LoggerFactory.getLogger(KarafSystemReady.class);

    private final ThreadFactory threadFactory = ThreadFactoryProvider.builder()
                                                    .namePrefix("SystemReadyService")
                                                    .logger(LOG)
                                                    .build().get();

    private final DelegatingSystemReadyMonitorMXBean mbean = new DelegatingSystemReadyMonitorMXBean(this);

    private final Config config;
    private final DiagProvider diagProvider;

    @Activate
    public KarafSystemReady(@Reference DiagProvider diagProvider, Config newConfig) {
        this.config = newConfig;
        mbean.registerMBean();
        this.diagProvider = requireNonNull(diagProvider);
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
        threadFactory.newThread(this::runCheckBundleDiag).start();
    }

    @Deactivate
    void deactivate() {
        this.mbean.unregisterMBean();
    }

    @VisibleForTesting
    DelegatingSystemReadyMonitorMXBean getMbean() {
        return mbean;
    }

    @SuppressWarnings("checkstyle:IllegalCatch") // below
    @SuppressFBWarnings(value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION", justification = "Re-thrown")
    private void runCheckBundleDiag() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            checkBundleDiagInfos(config.systemReadyTimeout(), TimeUnit.SECONDS,
                (timeInfo, bundleDiagInfos) -> {
                    LOG.info("checkBundleDiagInfos: Elapsed time {}s, remaining time {}s, {}",
                        timeInfo.getElapsedTimeInMS() / 1000, timeInfo.getRemainingTimeInMS() / 1000,
                        // INFRAUTILS-17: getSummaryText() instead getFullDiagnosticText() because people found log
                        //                confusing
                        bundleDiagInfos.getSummaryText());
                });

        } catch (SystemStateFailureException e) {
            LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", e);
            setSystemState(FAILURE);
            setSystemFailureCause(e);
            // We do not re-throw this

        } catch (RuntimeException throwable) {
            // It's exceptionally OK to catch RuntimeException here,
            // because we do want to set the currentFullSystemStatus
            LOG.error("Boot failed; not all SystemReadyListeners were not called, SystemState FAILURE", throwable);
            setSystemState(FAILURE);
            setSystemFailureCause(throwable);
            // and now we do re-throw it!
            throw throwable;
        }

        ready();
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
    private void checkBundleDiagInfos(long timeout, TimeUnit timeoutUnit,
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
