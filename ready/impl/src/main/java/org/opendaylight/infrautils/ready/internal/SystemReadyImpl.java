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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.karaf.bundle.core.BundleService;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyListenerRegistration;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.opendaylight.infrautils.utils.management.AbstractMXBean;
import org.opendaylight.odlparent.bundlestest.lib.SystemStateFailureException;
import org.opendaylight.odlparent.bundlestest.lib.TestBundleDiag;
import org.ops4j.pax.cdi.api.OsgiService;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the "system ready" service.
 *
 * @author Michael Vorburger.ch
 * @author Faseela K
 */
@Singleton
@OsgiServiceProvider(classes = SystemReadyMonitor.class)
public class SystemReadyImpl extends AbstractMXBean implements SystemReadyMonitor, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReadyImpl.class);

    private final Set<SystemReadyListener> listeners = new HashSet<>();
    private final AtomicReference<SystemState> currentSystemState = new AtomicReference<>(BOOTING);
    private static final String JMX_OBJECT_NAME = "SystemState";
    private static final String MBEAN_TYPE = "ready";

    private final ThreadFactory threadFactory = ThreadFactoryProvider.builder()
                                                    .namePrefix("SystemReadyService")
                                                    .logger(LOG)
                                                    .build().get();

    private final TestBundleDiag testBundleDiag;

    @Inject
    public SystemReadyImpl(BundleContext bundleContext, @OsgiService BundleService bundleService) {
        super(JMX_OBJECT_NAME, MBEAN_TYPE, null);
        super.registerMBean();
        this.testBundleDiag = new TestBundleDiag(bundleContext, bundleService);
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
    }

    @PostConstruct
    public void init() {
        threadFactory.newThread(this).start();
    }

    @PreDestroy
    public void stop() {
        super.unregisterMBean();
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void run() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            testBundleDiag.checkBundleDiagInfos(5, TimeUnit.MINUTES, (timeInfo, bundleDiagInfos) ->
                LOG.info("checkBundleDiagInfos: Elapsed time {}s, remaining time {}s, {}",
                    timeInfo.getElapsedTimeInMS() / 1000, timeInfo.getRemainingTimeInMS() / 1000,
                    // INFRAUTILS-17: getSummaryText() instead getFullDiagnosticText() because ppl found log confusing
                    bundleDiagInfos.getSummaryText()));

            LOG.info("System is ready");
            changeState(ACTIVE);

        } catch (SystemStateFailureException e) {
            LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", e);
            changeState(FAILURE);
            // We do not re-throw this

        } catch (RuntimeException throwable) {
            // It's exceptionally OK to catch RuntimeException here,
            // because we do want to set the currentFullSystemStatus
            LOG.error("Failed unexpectedly (SystemReadyListeners are not called)", throwable);
            changeState(FAILURE);
            // and now we do re-throw it!
            throw throwable;
        }
    }

    @Override
    public SystemState getSystemState() {
        return currentSystemState.get();
    }

    @Override
    public SystemReadyListenerRegistration registerListener(SystemReadyListener listener) {
        SystemState systemState = currentSystemState.get();
        SystemReadyListenerRegistration systemReadyListenerRegistration =
                new SystemReadyListenerRegistrationImpl(this, listener);
        synchronized (listeners) {
            listeners.add(Objects.requireNonNull(listener, "listener"));
        }
        listener.onSystemStateChange(systemState);
        return systemReadyListenerRegistration;
    }

    protected void unregisterListener(SystemReadyListener listener) {
        listeners.remove(listener);
    }

    private void changeState(SystemState newSate) {
        SystemReadyListener[] toNotify;
        synchronized (listeners) {
            toNotify = listeners.toArray(new SystemReadyListener[listeners.size()]);
            currentSystemState.set(newSate);
        }

        if (toNotify.length > 0) {
            LOG.info("Now notifying all its registered SystemReadyListeners...");
        }

        for (SystemReadyListener element : toNotify) {
            element.onSystemStateChange(newSate);
        }
    }

}
