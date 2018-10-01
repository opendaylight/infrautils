/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import static org.opendaylight.infrautils.ready.SystemState.ACTIVE;
import static org.opendaylight.infrautils.ready.SystemState.BOOTING;
import static org.opendaylight.infrautils.ready.SystemState.FAILURE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.ready.impl.SystemReadyMonitorMXBeanImpl;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.opendaylight.odlparent.bundlestest.lib.SystemStateFailureException;
import org.opendaylight.odlparent.bundlestest.lib.TestBundleDiag;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the "system ready" service for an OSGi Karaf environment.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
@Singleton
@Service(classes = SystemReadyMonitor.class)
public class KarafSystemReadyImpl implements SystemReadyMonitor, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KarafSystemReadyImpl.class);

    private final Queue<SystemReadyListener> listeners = new ConcurrentLinkedQueue<>();
    private final AtomicReference<SystemState> currentSystemState = new AtomicReference<>(BOOTING);
    private final AtomicReference<Throwable> currentSystemFailureCause = new AtomicReference<>();

    private final ThreadFactory threadFactory = ThreadFactoryProvider.builder()
                                                    .namePrefix("SystemReadyService")
                                                    .logger(LOG)
                                                    .build().get();

    private final TestBundleDiag testBundleDiag;

    @VisibleForTesting
    final SystemReadyMonitorMXBeanImpl mbean;

    @Inject
    public KarafSystemReadyImpl(BundleContext bundleContext, @Reference BundleService bundleService)
            throws JMException {
        this.mbean = new SystemReadyMonitorMXBeanImpl(this);
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

            SystemReadyListener[] toNotify;
            synchronized (listeners) {
                toNotify = listeners.toArray(new SystemReadyListener[listeners.size()]);
                currentSystemState.set(ACTIVE);
            }
            LOG.info("System ready; AKA: Aye captain, all warp coils are now operating at peak efficiency! [M.]");

            if (toNotify.length > 0) {
                LOG.info("Now notifying all its registered SystemReadyListeners...");
            }

            for (SystemReadyListener element : toNotify) {
                element.onSystemBootReady();
            }

        } catch (SystemStateFailureException e) {
            LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", e);
            currentSystemState.set(FAILURE);
            currentSystemFailureCause.set(e);
            // We do not re-throw this

        } catch (RuntimeException throwable) {
            // It's exceptionally OK to catch RuntimeException here,
            // because we do want to set the currentFullSystemStatus
            LOG.error("Boot failed; not all SystemReadyListeners were not called, SystemState FAILURE", throwable);
            currentSystemState.set(FAILURE);
            currentSystemFailureCause.set(throwable);
            // and now we do re-throw it!
            throw throwable;
        } catch (Exception e) {
            LOG.error("SystemReadyListener.onSystemBootReady() threw Exception; "
                    + "other SystemReadyListeners not called; SystemState FAILURE", e);
            currentSystemState.set(FAILURE);
            currentSystemFailureCause.set(e);
            LOG.error("Failed unexpectedly (SystemReadyListeners are not called)", e);
            // really no point in re-throwing it
        }
    }

    @Override
    public SystemState getSystemState() {
        return currentSystemState.get();
    }

    @Override
    public String getFailureCause() {
        return Optional.ofNullable(currentSystemFailureCause.get())
                .map(throwable -> Throwables.getStackTraceAsString(throwable)).orElse("");
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void registerListener(SystemReadyListener listener) {
        SystemState state;
        synchronized (listeners) {
            state = currentSystemState.get();
            if (state == BOOTING) {
                listeners.add(listener);
            }
        }

        if (state == ACTIVE) {
            try {
                listener.onSystemBootReady();
            } catch (Exception e) {
                LOG.error("SystemReadyListener.onSystemBootReady() threw Exception; "
                        + "but state was already ACTIVE, going back to FAILURE now", e);
                currentSystemState.set(FAILURE);
                currentSystemFailureCause.set(e);
                // Do not re-throw.
            }
        }
    }
}
