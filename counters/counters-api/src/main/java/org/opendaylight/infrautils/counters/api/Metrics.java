/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.counters.api;

import com.codahale.metrics.MetricRegistry;

public interface Metrics {

    /**
     *  increments the given counter
     * @param group     Group name
     * @param type      Type of this counter
     * @param subType   Subtype of this counter
     */
    void incrementCounter(String group, String type, String subType);

    /**
     * Marks the given meter
     * @param group     Group name for this meter
     * @param type      Type of this meter
     * @param subType   sub type of this meter
     */
    void markMeter(String group, String type, String subType);

    /**
     * Get an instance of metrics registry
     * @return returns instance of metrics registry
     */
    MetricRegistry getRegistry();

}
