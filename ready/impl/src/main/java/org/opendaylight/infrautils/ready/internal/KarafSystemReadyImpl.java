/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import static org.opendaylight.infrautils.ready.SystemState.FAILURE;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.apache.karaf.bundle.core.BundleService;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.spi.DelegatingSystemReadyMonitorMXBean;
import org.opendaylight.infrautils.ready.spi.SimpleSystemReadyMonitor;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.opendaylight.odlparent.bundlestest.lib.SystemStateFailureException;
import org.opendaylight.odlparent.bundlestest.lib.TestBundleDiag;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SystemReadyMonitor} implementation for an OSGi Karaf environment.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
@Singleton
@Service(classes = SystemReadyMonitor.class)
public class KarafSystemReadyImpl extends SimpleSystemReadyMonitor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KarafSystemReadyImpl.class);

    private final ThreadFactory threadFactory = ThreadFactoryProvider.builder()
                                                    .namePrefix("SystemReadyService")
                                                    .logger(LOG)
                                                    .build().get();

    private final TestBundleDiag testBundleDiag;

    private final DelegatingSystemReadyMonitorMXBean mbean;

    @Inject
    public KarafSystemReadyImpl(BundleContext bundleContext, @Reference BundleService bundleService)
            throws JMException {
        this.mbean = new DelegatingSystemReadyMonitorMXBean(this);
        this.mbean.registerMBean();
        this.testBundleDiag = new TestBundleDiag(bundleContext, bundleService);
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
    }

    @PostConstruct
    public void init() {
        threadFactory.newThread(this).start();
    }

    @PreDestroy
    public void stop() throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
        this.mbean.unregisterMBean();
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch") // below
    public void run() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            testBundleDiag.checkBundleDiagInfos(5, TimeUnit.MINUTES, (timeInfo, bundleDiagInfos) ->
                LOG.info("checkBundleDiagInfos: Elapsed time {}s, remaining time {}s, {}",
                    timeInfo.getElapsedTimeInMS() / 1000, timeInfo.getRemainingTimeInMS() / 1000,
                    // INFRAUTILS-17: getSummaryText() instead getFullDiagnosticText() because ppl found log confusing
                    bundleDiagInfos.getSummaryText()));

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
