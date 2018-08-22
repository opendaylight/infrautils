/*
 * Copyright (c) 2016, 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.test;

import com.google.inject.TypeLiteral;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;
import org.opendaylight.infrautils.diagstatus.ServiceStatusProvider;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceImpl;
import org.opendaylight.infrautils.diagstatus.internal.DiagStatusServiceMBeanImpl;
import org.opendaylight.infrautils.inject.guice.testutils.AbstractGuiceJsr250Module;
import org.opendaylight.infrautils.ready.SystemReadyListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.SystemState;
import org.ops4j.pax.cdi.api.OsgiService;

/**
 * Dependency Injection Wiring for {@link DiagStatusTest}.
 *
 * @author Faseela K & Michael Vorburger.ch
 */
public class DiagStatusTestModule extends AbstractGuiceJsr250Module {

    @Override
    protected void configureBindings() throws UnknownHostException {
        bind(DiagStatusService.class).to(DiagStatusServiceImpl.class);
        bind(new TypeLiteral<List<ServiceStatusProvider>>() {}).toInstance(Collections.emptyList());
        bind(SystemReadyMonitor.class).annotatedWith(OsgiService.class).toInstance(new SystemReadyMonitor() {

            @Override
            public void registerListener(SystemReadyListener listener) {
                // NOOP
            }

            @Override
            public SystemState getSystemState() {
                return SystemState.ACTIVE;
            }

            @Override
            public Optional<Throwable> getFailureCause() {
                return Optional.empty();
            }
        });
        bind(DiagStatusServiceMBean.class).to(DiagStatusServiceMBeanImpl.class);
    }
}
