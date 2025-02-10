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
        // Does something similar to Karaf's "diag" CLI command, and throws a {@link SystemStateFailureException} if
        // anything including bundle wiring is not OK.
        //
        // The implementation is based on Karaf's BundleService, and not the BundleStateService, because each Karaf
        // supported DI system (such as Blueprint and Declarative Services, see String constants in BundleStateService),
        // will have a separate BundleStateService.  The BundleService however will contain the combined status of all
        // BundleStateServices.
        var timeoutNanos = TimeUnit.SECONDS.toNanos(config.systemReadyTimeout());
        var started = System.nanoTime();
        LOG.info("checkBundleDiagInfos() started...");

        while (true) {
            var elapsedNanos = System.nanoTime() - started;
            var remainingNanos = timeoutNanos - elapsedNanos;
            var diag = diagProvider.currentDiag();

            SystemStateFailureException cause;
            try {
                var bundleInfos = BundleDiagInfos.ofDiag(diag);

                var systemState = bundleInfos.getSystemState();
                switch (systemState) {
                    case Active -> {
                        // Inform the developer of the green SystemState.Active
                        LOG.info("checkBundleDiagInfos: Elapsed time {}s, remaining time {}s, {}",
                            TimeUnit.NANOSECONDS.toSeconds(elapsedNanos),
                            TimeUnit.NANOSECONDS.toSeconds(remainingNanos),
                            // Note:: getSummaryText() instead getFullDiagnosticText() because people found log
                            //        confusing
                            bundleInfos.getSummaryText());
                        ready();
                        return;
                    }
                    case Failure ->
                        cause = new SystemStateFailureException("diag failed; some bundles failed to start",
                            bundleInfos);
                    case Stopping ->
                        cause = new SystemStateFailureException("diag failed; some bundles are stopping", bundleInfos);

                    // FIXME: a.k.a Booting, but the combination of checkstyle and error-prone requires us to use this
                    //        ugly construct. Reasons:
                    //        - we need either a 'default' or 'case null' for checkstyle
                    //        - using a catch-all 'default' is hated by error-prone
                    //        - using a 'case null' without 'default' results in
                    //          https://github.com/google/error-prone/issues/4721
                    //        So when we have fixed error-prone, we need to go back to 'case Booting' and 'case null'.
                    default -> {
                        if (remainingNanos > 0) {
                            try {
                                Thread.sleep(1000);
                                continue;
                            } catch (InterruptedException e) {
                                cause = new SystemStateFailureException("Interrupted waiting for a retry", bundleInfos,
                                    e);
                            }
                        } else {
                            // This typically happens due to bundles still in BundleState GracePeriod or Waiting
                            cause = new SystemStateFailureException("diag timeout; some bundles are still not active:",
                                bundleInfos);
                        }
                    }
                }
            } catch (RuntimeException e) {
                // It's exceptionally OK to catch RuntimeException here,
                // because we do want to set the currentFullSystemStatus
                LOG.error("Boot failed; not all SystemReadyListeners were not called, SystemState FAILURE", e);
                setSystemState(FAILURE);
                setSystemFailureCause(e);
                // and now we do re-throw it!
                throw e;
            }

            LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", cause);
            diag.logState(LOG);
            setSystemState(FAILURE);
            setSystemFailureCause(cause);
            ready();
            return;
        }
    }
}
