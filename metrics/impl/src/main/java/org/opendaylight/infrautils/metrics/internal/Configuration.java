/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import com.google.common.base.MoreObjects;

/**
 * Configuration properties for the metrics implementation.
 *
 * <p>Karaf's OSGi ConfigAdmin service, via the cm blueprint extension, sets this
 * from the etc/org.opendaylight.infrautils.metrics.cfg configuration file.
 *
 * @author Michael Vorburger.ch
 */
public class Configuration {

    private final MetricProviderImpl metricProvider;

    private int threadsDeadlockWatcherIntervalMS;

    public Configuration(MetricProviderImpl metricProvider) {
        this.metricProvider = metricProvider;
    }

    public void setThreadsDeadlockWatcherIntervalMS(int ms) {
        this.threadsDeadlockWatcherIntervalMS = ms;
        metricProvider.updateConfiguration(this);
    }

    public int getThreadsDeadlockWatcherIntervalMS() {
        return this.threadsDeadlockWatcherIntervalMS;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("threadsDeadlockWatcherIntervalMS", threadsDeadlockWatcherIntervalMS)
                .toString();
    }

}
