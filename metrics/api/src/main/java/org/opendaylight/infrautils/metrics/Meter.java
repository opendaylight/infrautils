/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

/**
 * A meter metric which measures throughput.
 */
public interface Meter extends CloseableMetric {

    /**
     * Mark the occurrence of an event.
     */
    default void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     */
    void mark(long howMany);

}
