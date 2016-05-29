/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.counters.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.infrautils.utils.CompareUtil;
import org.opendaylight.infrautils.utils.StringUtil;
import org.opendaylight.infrautils.utils.types.UnsignedLong;

public class OccurenceCounter implements Comparable<OccurenceCounter> {
    private AtomicLong totalOccur = new AtomicLong(0);
    private AtomicInteger occursSinceLastPrint = new AtomicInteger(0);
    private AtomicInteger occursInLastBulk = new AtomicInteger(0);
    public String name;
    private boolean isErasable = true;
    public boolean isLoggable;
    public boolean isState = false;;

    public String group;
    public String groupAcronym;
    private String description;
    private OccurenceCounter parent;

    public static CountersGroup getCounterGroups(String[] filterGroupNames, String[] filterCounterNames) {
        CountersGroup groups = new CountersGroup();
        synchronized (counters) {
            for (OccurenceCounter counter : counters) {
                if ((counter != null) && counter.isMatching(filterGroupNames, filterCounterNames)) {
                    Map<String, UnsignedLong> groupCounters = groups.get(counter.group);
                    if (groupCounters == null) {
                        groupCounters = new HashMap<String, UnsignedLong>();
                        groups.put(counter.group, groupCounters);
                    }
                    groupCounters.put(counter.name, UnsignedLong.valueOf(counter.get()));
                }
            }
        }
        return groups;
    }

    public static void clearAllCounters(String[] filterGroupNames, String[] filterCounterNames) {
        synchronized (counters) {
            for (OccurenceCounter counter : counters) {
                if ((counter != null) && counter.isErasable && counter.isMatching(filterGroupNames, filterCounterNames)) {
                    counter.internalResetCounter();
                }
            }
        }
    }

    public boolean isMatching(String[] filterGroupNames, String[] filterCounterNames) {
        return StringUtil.isMatching(group, filterGroupNames) && StringUtil.isMatching(name, filterCounterNames);
    }

    private static HashSet<OccurenceCounter> counters = new HashSet<OccurenceCounter>();

    public OccurenceCounter(String group, String name, boolean isErasable, String description, OccurenceCounter parent, boolean isLoggable,
            boolean isState) {
        this(group, name, description, isErasable, parent, isLoggable, isState);
    }

    public OccurenceCounter(String group, String name, String description) {
        this(group, name, true, description, null, true, false);
    }

    public OccurenceCounter(String group, String name, String description, OccurenceCounter parent) {
        this(group, name, true, description, parent, true, false);
    }

    public OccurenceCounter(String group, String name, String description, boolean isLoggable, boolean isState) {
        this(group, name, true, description, null, isLoggable, isState);
    }

    public OccurenceCounter(String group, String name, boolean isErasable, String description, boolean isLoggable) {
        this(group, name, isErasable, description, null, isLoggable, false);
    }

    public OccurenceCounter(String group, String name, String description, boolean isErasable, OccurenceCounter parent,
            boolean isLoggable, boolean isState) {
        // Adds group acronym; removes all lower case characters from group name and changes the upper case characters to lower case
        this(group, group.replaceAll("[^A-Z]", "").toLowerCase(), name, description, isErasable, parent, true, false);
    }
    
    public OccurenceCounter(String group, String groupAcronym, String name, String description, boolean isErasable, OccurenceCounter parent, //
            boolean isLoggable, boolean isState) {
        this.description = description;
        this.parent = parent;
        this.isErasable = isErasable;
        this.isLoggable = isLoggable;
        this.group = group;
        this.groupAcronym = groupAcronym;
        this.name = name;
        this.isState = isState;
        internalResetCounter();
        synchronized (counters) {
            counters.add(this);
        }
    }

    public static HashSet<OccurenceCounter> getCounters() {
        return counters;
    }

    public static HashSet<OccurenceCounter> cloneCounters() {
        synchronized (counters) {
            return new HashSet<OccurenceCounter>(counters);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((group == null) ? 0 : group.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
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
        OccurenceCounter other = (OccurenceCounter) obj;
        return CompareUtil.safeCompare(other.name, name) && CompareUtil.safeCompare(other.group, group);
    }

    public void resetCounter() {
        if (parent != null) {
            throw new UnsupportedOperationException("Cannot reset a counter with parent");
        }
        internalResetCounter();
        synchronized (counters) {
            for (OccurenceCounter counter : counters) {
                if ((counter != null) && (counter.parent == this)) {
                    counter.internalResetCounter();
                }
            }
        }
    }

    private void internalResetCounter() {
        totalOccur.set(0);
        occursSinceLastPrint.set(0);
        occursInLastBulk.set(0);
    }

    public long add(long val) {
        if (parent != null) {
            parent.add(val);
        }
        return totalOccur.addAndGet(val);
    }

    public void set(long val) {
        if (parent != null) {
            throw new UnsupportedOperationException("Cannot set value for a counter with parent");
        }
        totalOccur.set(val);
    }

    public long inc() {
        if (parent != null) {
            parent.add(1);
        }
        occursSinceLastPrint.addAndGet(1);
        occursInLastBulk.addAndGet(1);
        return totalOccur.addAndGet(1);
    }

    public long get() {
        return totalOccur.longValue();
    }

    public long dec() {
        return add(-1);
    }

    @Override
    public String toString() {
        return group + "." + name + "#" + totalOccur.toString();
    }

    public int compareTo(OccurenceCounter o) {
        int groupCompare = group.compareTo(o.group);
        if (groupCompare != 0) {
            return groupCompare;
        }
        return name.compareTo(o.name);
    }

    public String getDescription() {
        return description;
    }
}
