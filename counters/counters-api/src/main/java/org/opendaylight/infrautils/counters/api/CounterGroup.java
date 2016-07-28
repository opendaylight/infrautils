/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.counters.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.infrautils.utils.types.UnsignedLong;



public class CounterGroup {

    private final String groupName;

    private final List<CounterGroupEntry> counters = new ArrayList<CounterGroupEntry>();

    protected CounterGroup() {
        groupName = "";
    }

    public CounterGroup(Entry<String, Map<String, UnsignedLong>> counterGroupEntry) {
        groupName = counterGroupEntry.getKey();
        for (Map.Entry<String, UnsignedLong> e : counterGroupEntry.getValue().entrySet()) {
            counters.add(new CounterGroupEntry(e));
        }
    }

    private static class CounterGroupEntry {
        private final String key;
        private final BigInteger value;

        protected CounterGroupEntry() {
            key = "";
            value = null;
        }

        protected CounterGroupEntry(Map.Entry<String, UnsignedLong> e) {
            key = e.getKey();
            value = e.getValue().bigIntegerValue();
        }
    }
}