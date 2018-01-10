/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

/**
 * Meter metric, which measures throughput.
 *
 * <p>Note that this with <tt>mark()</tt> measures the rate at which a set of events occur;
 * whereas {@link Counter} is for things which will <tt>increase()</tt> - and can also <tt>decrease()</tt>.
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
