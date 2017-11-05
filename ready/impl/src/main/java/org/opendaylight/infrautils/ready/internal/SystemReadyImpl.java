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

import org.apache.karaf.bundle.core.BundleService;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider;
import org.opendaylight.infrautils.utils.management.MBeanUtils;
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
public class SystemReadyImpl implements SystemReadyMonitor, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReadyImpl.class);

    private final Queue<SystemReadyListener> listeners = new ConcurrentLinkedQueue<>();
    private final AtomicReference<SystemState> currentSystemState = new AtomicReference<>(BOOTING);
    private static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.ready:type=SystemState";

    private final ThreadFactory threadFactory = ThreadFactoryProvider.builder()
                                                    .namePrefix("SystemReadyService")
                                                    .logger(LOG)
                                                    .build().get();

    private final TestBundleDiag testBundleDiag;

    @Inject
    public SystemReadyImpl(BundleContext bundleContext, @OsgiService BundleService bundleService)
            throws JMException {
        MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
        this.testBundleDiag = new TestBundleDiag(bundleContext, bundleService);
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
    }

    @PostConstruct
    public void init() {
        threadFactory.newThread(this).start();
    }

    @PreDestroy
    public void stop() throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
        MBeanUtils.unregisterServerMBean(this, JMX_OBJECT_NAME);
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch") // below
    public void run() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            testBundleDiag.checkBundleDiagInfos(5, TimeUnit.MINUTES /* TODO c/56750 , this */);
            currentSystemState.set(ACTIVE);
            LOG.info("System ready; AKA: Aye captain, all warp coils are now operating at peak efficiency! [M.]");

            if (!listeners.isEmpty()) {
                LOG.info("Now notifying all its registered SystemReadyListeners...");
            }

            // TODO functionalityReadyNotifier.register(BundlesBootedReady.class);

            SystemReadyListener listener;
            while ((listener = listeners.poll()) != null) {
                listener.onSystemBootReady();
            }

        } catch (SystemStateFailureException e) {
            LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", e);
            currentSystemState.set(FAILURE);
            // We do not re-throw this

        } catch (RuntimeException throwable) {
            // It's exceptionally OK to catch RuntimeException here,
            // because we do want to set the currentFullSystemStatus
            LOG.error("Failed unexpectedly (SystemReadyListeners are not called)", throwable);
            currentSystemState.set(FAILURE);
            // and now we do re-throw it!
            throw throwable;
        }
    }

    @Override
    public SystemState getSystemState() {
        return currentSystemState.get();
    }

    @Override
    public void registerListener(SystemReadyListener listener) {
        listeners.add(listener);
    }
}
