/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.spi;

import org.opendaylight.infrautils.ready.SystemReadyMonitorMXBean;
import org.opendaylight.infrautils.ready.SystemState;
import org.opendaylight.infrautils.utils.management.AbstractMXBean;

/**
 * SystemReadyMonitorMXBean implementation.
 *
 * @author Michael Vorburger.ch, based on code by Faseela originally in KarafSystemReady
 */
public class DelegatingSystemReadyMonitorMXBean extends AbstractMXBean implements SystemReadyMonitorMXBean {

    private static final String JMX_OBJECT_NAME = "SystemState";
    private static final String MBEAN_TYPE = "ready";

    private final SystemReadyMonitorMXBean delegate;

    public DelegatingSystemReadyMonitorMXBean(SystemReadyMonitorMXBean delegate) {
        super(JMX_OBJECT_NAME, MBEAN_TYPE, null);
        this.delegate = delegate;
    }

    @Override
    public SystemState getSystemState() {
        return delegate.getSystemState();
    }

    @Override
    public String getFailureCause() {
        return delegate.getFailureCause();
    }
}
