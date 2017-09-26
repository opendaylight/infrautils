/*
 * Copyright (c) 2016, 2017 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.counters.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.infrautils.utils.types.UnsignedLong;

public class OccurenceCounter implements Comparable<OccurenceCounter> {
    private final AtomicLong totalOccur = new AtomicLong(0);
    private final AtomicInteger occursSinceLastPrint = new AtomicInteger(0);
    private final AtomicInteger occursInLastBulk = new AtomicInteger(0);
    public final String name;
    private boolean isErasable = true;
    public final boolean isLoggable;
    public boolean isState = false;

    public final String group;
    public final String groupAcronym;
    private final String description;
    private final OccurenceCounter parent;

    public OccurenceCounter(String group, String name, boolean isErasable, String description, OccurenceCounter parent,
            boolean isLoggable, boolean isState) {
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
        // Adds group acronym; removes all lower case characters from group name
        // and changes the upper case characters to lower case
        this(group, group.replaceAll("[^A-Z]", "").toLowerCase(), name, description, isErasable, parent, isLoggable,
                isState);
    }

    public OccurenceCounter(String group, String groupAcronym, String name, String description, boolean isErasable,
            OccurenceCounter parent, //
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
        synchronized (COUNTERS) {
            COUNTERS.add(this);
        }
    }

    public static CountersGroup getCounterGroups(String[] filterGroupNames, String[] filterCounterNames) {
        CountersGroup groups = new CountersGroup();
        synchronized (COUNTERS) {
            for (OccurenceCounter counter : COUNTERS) {
                if (counter != null && counter.isMatching(filterGroupNames, filterCounterNames)) {
                    Map<String, UnsignedLong> groupCounters = groups.get(counter.group);
                    if (groupCounters == null) {
                        groupCounters = new HashMap<>();
                        groups.put(counter.group, groupCounters);
                    }
                    groupCounters.put(counter.name, UnsignedLong.valueOf(counter.get()));
                }
            }
        }
        return groups;
    }

    public static void clearAllCounters(String[] filterGroupNames, String[] filterCounterNames) {
        synchronized (COUNTERS) {
            for (OccurenceCounter counter : COUNTERS) {
                if (counter != null && counter.isErasable && counter.isMatching(filterGroupNames, filterCounterNames)) {
                    counter.internalResetCounter();
                }
            }
        }
    }

    public boolean isMatching(String[] filterGroupNames, String[] filterCounterNames) {
        return filterGroupNames != null && filterCounterNames != null && Arrays.stream(filterGroupNames).anyMatch(
                group::matches) && Arrays.stream(filterCounterNames).anyMatch(name::matches);
    }

    private static final HashSet<OccurenceCounter> COUNTERS = new HashSet<>();

    public static HashSet<OccurenceCounter> getCounters() {
        return COUNTERS;
    }

    public static HashSet<OccurenceCounter> cloneCounters() {
        synchronized (COUNTERS) {
            return new HashSet<>(COUNTERS);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (group == null ? 0 : group.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
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
        return Objects.equals(other.name, name) && Objects.equals(other.group, group);
    }

    public void resetCounter() {
        if (parent != null) {
            throw new UnsupportedOperationException("Cannot reset a counter with parent");
        }
        internalResetCounter();
        synchronized (COUNTERS) {
            for (OccurenceCounter counter : COUNTERS) {
                if (counter != null && counter.parent == this) {
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

    private long add(long val) {
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

    @Override
    public int compareTo(OccurenceCounter occurenceCounter) {
        int groupCompare = group.compareTo(occurenceCounter.group);
        if (groupCompare != 0) {
            return groupCompare;
        }
        return name.compareTo(occurenceCounter.name);
    }

    public String getDescription() {
        return description;
    }
}
