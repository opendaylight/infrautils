/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.guice;

import com.google.inject.AbstractModule;
import org.opendaylight.infrautils.inject.PostFullSystemInjectionListener;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.infrautils.ready.spi.SimpleSystemReadyMonitor;

public class ReadyModule extends AbstractModule implements PostFullSystemInjectionListener {

    private final SimpleSystemReadyMonitor systemReadyMonitor = new SimpleSystemReadyMonitor();

    @Override
    protected void configure() {
        bind(SystemReadyMonitor.class).toInstance(systemReadyMonitor);
        bind(PostFullSystemInjectionListener.class).toInstance(this);
    }

    @Override
    public void onFullSystemInjected() {
        systemReadyMonitor.ready();
    }
}
