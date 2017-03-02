/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.counters.impl.service;

import org.opendaylight.infrautils.counters.api.OccurenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;


public class OccurenceCounterEntry {
    public OccurenceCounter counter;
    public long lastVal;
    public long maxWidth = 0;
    public Logger logger;
    public String printName;    

    public OccurenceCounterEntry(OccurenceCounter counter) {
        this.counter = counter;
        lastVal = 0;
        logger = LoggerFactory.getLogger(counter.group + "." + counter.name);
        printName = counter.groupAcronym + "." + counter.name;
        
        MetricRegistry r = MetricRegistry.name("yakir", "a");
        r.
        
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + counter.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OccurenceCounterEntry other = (OccurenceCounterEntry) obj;
        if (counter == null) {
            if (other.counter != null) {
                return false;
            }
        } else if (!counter.equals(other.counter)) {
            return false;
        }
        return true;
    }
}
