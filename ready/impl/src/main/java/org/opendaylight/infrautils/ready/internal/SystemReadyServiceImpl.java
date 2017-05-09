/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import com.google.common.base.Throwables;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyService;
import org.opendaylight.infrautils.utils.concurrent.Executors;
import org.opendaylight.odlparent.bundles4test.TestBundleDiag;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of SystemReadyService.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = SystemReadyService.class)
public class SystemReadyServiceImpl implements SystemReadyService, Runnable /* TODO , DiagUpdatesListener */ {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReadyServiceImpl.class);

    private final Queue<SystemReadyListener> listeners = new ConcurrentLinkedQueue<>();
    private final AtomicReference<String> currentFullSystemStatus = new AtomicReference<>("EARLY-BOOT");

    private final BundleContext bundleContext;
    private final ExecutorService executorService;

    @Inject
    public SystemReadyServiceImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        executorService = Executors.newSingleThreadExecutor("SystemReadyService", LOG);
        executorService.submit(this);
        LOG.info("SystemReadyService will now start providing full system readyness status updates...");
    }

    @Override
    public void run() {
        try {
            // 5 minutes really ought to be enough for the whole circus to completely boot up?!
            TestBundleDiag.checkBundleDiagInfos(bundleContext, 5, TimeUnit.MINUTES /* TODO , this */);
            currentFullSystemStatus.set(SystemReadyService.FULLY_READY);

            LOG.info("SystemReadyService will now notify all its registered SystemReadyListeners...");
            SystemReadyListener listener;
            while ((listener = listeners.poll()) != null) {
                listener.onSystemReady();
            }

        } catch (Throwable throwable) {
            LOG.error("SystemReadyService failed (registered SystemReadyListeners are not called)", throwable);
            currentFullSystemStatus.set(Throwables.getStackTraceAsString(throwable));
        }
    }

/* TODO uncomment when TBD is merged in odlparent

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
    public String getFullSystemStatus() {
        return currentFullSystemStatus.get();
    }

    @Override
    public void registerListener(SystemReadyListener listener) {
        listeners.add(listener);
    }

}
