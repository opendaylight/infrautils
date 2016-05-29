/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.counters.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.infrautils.utils.types.UnsignedLong;


@SuppressWarnings("serial")
public class CountersGroup implements Serializable {

    private final Map<String, Map<String, UnsignedLong>> groups = new TreeMap<String, Map<String, UnsignedLong>>();

    public List<CounterGroup> getCounters() {
        List<CounterGroup> ret = new ArrayList<CounterGroup>();
        for (Map.Entry<String, Map<String, UnsignedLong>> e : groups.entrySet()) {
            ret.add(new CounterGroup(e));
        }
        return ret;
    }

    public CountersGroup() {
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public void put(String groupName, Map<String, UnsignedLong> counters) {
        if (groupName == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (counters == null) {
            throw new IllegalArgumentException("counters cannot be null or empty");
        }
        groups.put(groupName, counters);
    }

    public CountersGroup rename(String oldGroupName, String newGroupName) {
        Map<String, UnsignedLong> group = groups.remove(oldGroupName);
        if (group == null) {
            return null;
        }
        groups.put(newGroupName, group);
        return this;
    }

    public CountersGroup put(String groupName, String counterName, UnsignedLong counterValue) {
        Map<String, UnsignedLong> group = groups.get(groupName);
        if (group == null) {
            group = new HashMap<String, UnsignedLong>();
            groups.put(groupName, group);
        }
        group.put(counterName, counterValue);
        return this;
    }

    public Set<String> getGroupsNames() {
        return groups.keySet();
    }

    public Map<String, UnsignedLong> get(String groupName) {
        return groups.get(groupName);
    }

    @Override
    public String toString() {
        return "CounterGroups " + groups;
    }

    @Override
    public int hashCode() {
        return groups.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj == null) || !getClass().equals(obj.getClass())) {
            return false;
        }
        return groups.equals(((CountersGroup) obj).groups);
    }

    public String asHierarchy(boolean removePollTimestamp) {
        StringBuilder s = new StringBuilder("CounterGroups");
        for (Entry<String, Map<String, UnsignedLong>> groupEntry : groups.entrySet()) {
            s.append("\n\t").append(groupEntry.getKey());
            for (Entry<String, UnsignedLong> counterEntry : groupEntry.getValue().entrySet()) {
                if (!removePollTimestamp || !"poll-time-stamp".equals(counterEntry.getKey())) {
                    s.append("\n\t\t").append(counterEntry);
                }
            }
        }
        return s.toString();
    }
}