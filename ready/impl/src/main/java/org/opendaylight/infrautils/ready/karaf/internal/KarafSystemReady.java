/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.karaf.internal;

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
import org.opendaylight.odlparent.bundlestest.lib.SystemStateFailureException;
import org.opendaylight.odlparent.bundlestest.lib.TestBundleDiag;
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
    private final TestBundleDiag testBundleDiag;

    @Activate
    public KarafSystemReady(@Reference DiagProvider diagProvider, Config newConfig) {
        this.config = newConfig;
        mbean.registerMBean();
        testBundleDiag = new TestBundleDiag(diagProvider);
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
        threadFactory.newThread(this::runCheckBundleDiag).start();
    }

    @Deactivate
    void deactivate() {
        this.mbean.unregisterMBean();
    }

    @SuppressWarnings("checkstyle:IllegalCatch") // below
    @SuppressFBWarnings(value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION", justification = "Re-thrown")
    private void runCheckBundleDiag() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            testBundleDiag.checkBundleDiagInfos(config.systemReadyTimeout(), TimeUnit.SECONDS,
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

    @VisibleForTesting
    DelegatingSystemReadyMonitorMXBean getMbean() {
        return mbean;
    }
}
