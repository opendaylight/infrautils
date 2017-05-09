/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import static org.opendaylight.infrautils.ready.SystemState.Active;
import static org.opendaylight.infrautils.ready.SystemState.Booting;
import static org.opendaylight.infrautils.ready.SystemState.Failure;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.utils.concurrent.Executors;
import org.opendaylight.odlparent.bundles4test.TestBundleDiag;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of "system ready" services.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = SystemReadyMonitor.class)
public class SystemReadyImpl implements SystemReadyMonitor, Runnable /* TODO c/56750 , DiagUpdatesListener */ {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReadyImpl.class);

    private final Queue<SystemReadyListener> listeners = new ConcurrentLinkedQueue<>();
    private final AtomicReference<SystemState> currentSystemState = new AtomicReference<>(Booting);

    private final BundleContext bundleContext;
    private final ExecutorService executorService;

    @Inject
    public SystemReadyImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        executorService = Executors.newSingleThreadExecutor("SystemReadyService", LOG);
        CompletableFuture.runAsync(this, executorService).exceptionally(throwable -> {
            LOG.error("Exception in background thread", throwable);
            return null;
        });
        LOG.info("Now starting to provide full system readiness status updates (see TestBundleDiag's logs)...");
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void run() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            TestBundleDiag.checkBundleDiagInfos(bundleContext, 5, TimeUnit.MINUTES /* TODO c/56750 , this */);
            currentSystemState.set(Active);
            LOG.info("System ready; AKA: Aye captain, all warp coils are now operating at peak efficiency! [M.]");

            if (!listeners.isEmpty()) {
                LOG.info("Now notifying all its registered SystemReadyListeners...");
            }
            SystemReadyListener listener;
            while ((listener = listeners.poll()) != null) {
                listener.onSystemBootReady();
            }

// TODO uncomment when https://git.opendaylight.org/gerrit/#/c/56817/ is merged in odlparent..
//        } catch (SystemStateFailureException e) {
//          LOG.error("Failed, some bundles did not start (SystemReadyListeners are not called)", e);
//            currentSystemState.set(Failure);
//            // We do not re-throw this

        } catch (Throwable throwable) {
            // It's exceptionally OK to catch Throwable here, because we do want to set the currentFullSystemStatus
            LOG.error("Failed unexpectedly (SystemReadyListeners are not called)", throwable);
            currentSystemState.set(Failure);
            throw throwable; // we do re-throw this
        }
    }

/* TODO uncomment when https://git.opendaylight.org/gerrit/#/c/56750/ is merged in odlparent..

    @Override
    public void onUpdate(String diagInfo) {
        currentFullSystemStatus.set(diagInfo);
    }
*/
    @PreDestroy
    public void close() {
        executorService.shutdownNow();
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
