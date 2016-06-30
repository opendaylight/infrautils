/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.counters.impl.service;

import java.util.HashSet;
import java.util.LinkedHashSet;

import org.opendaylight.infrautils.counters.impl.OccurenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountersDumperThread implements Runnable {
    private Object blockResetCounters = new Object();
    private volatile boolean keepRunning = true;
    private volatile int countersDumpInterval;
    private static HashSet<OccurenceCounterEntry> counters = new HashSet<OccurenceCounterEntry>();
    private static LinkedHashSet<OccurenceCounterEntry> printCounters = new LinkedHashSet<OccurenceCounterEntry>();
    protected static final Logger logger = LoggerFactory.getLogger(CountersDumperThread.class);

    public CountersDumperThread(int countersDumpInterval) throws Exception {
        this.countersDumpInterval = countersDumpInterval;
        updateCounters();
    }

    public void clearAllCounters(String[] filterGroupNames, String[] filterCounterNames) {
        synchronized (blockResetCounters) {
            OccurenceCounter.clearAllCounters(filterGroupNames, filterCounterNames);
            for (OccurenceCounterEntry counter : counters) {
                if (counter.counter.isMatching(filterGroupNames, filterCounterNames)) {
                    counter.lastVal = 0;
                }
            }
        }
    }

    private void updateCounters() {
        HashSet<OccurenceCounter> clonedCounters = OccurenceCounter.cloneCounters();
        for (OccurenceCounter counter : clonedCounters) {
            if (!counter.isLoggable) {
                continue;
            }
            counters.add(new OccurenceCounterEntry(counter));
        }
    }

    private void runCounterDump() {
        synchronized (blockResetCounters) {
            if (counters.size() != OccurenceCounter.getCounters().size()) {
                updateCounters();
            }
            StringBuilder sb = new StringBuilder();
            for (OccurenceCounterEntry entry : counters) {
                if ((entry.counter.get() != entry.lastVal) && isCategoryPermitted(entry)) {
                    printCounters.add(entry);
                } else {
                    printCounters.remove(entry);
                }
            }
            for (OccurenceCounterEntry entry : printCounters) {
                long curVal = entry.counter.get();
                long difference = curVal - entry.lastVal;
                updateMaxWidth(entry, difference);
                entry.lastVal = curVal;
                sb.append(entry.printName).append(": ");
                if (!entry.counter.isState) {
                    if (difference > 0) {
                        sb.append("+");
                    }
                    sb.append(difference);
                } else {
                    sb.append(curVal);
                }
                alignToMaxWidth(entry, difference, sb);
                sb.append(", ");
            }
            if (sb.length() > 0) {
                sb.delete(sb.length() - 2, sb.length());
                logger.info(sb.toString());
            }
        }
    }

    public void run() {
        logger.info("Starting counters thread with interval of: {}", countersDumpInterval);
        while (keepRunning) {
            try {
                try {
                    runCounterDump();
                    Thread.sleep(countersDumpInterval);
                } catch (InterruptedException e) {
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
        logger.info("Killed counters thread");
    }

    private void alignToMaxWidth(OccurenceCounterEntry entry, long difference, StringBuilder sb) {
        int width = Long.toString(difference).length();
        if (entry.maxWidth > width) {
            for (int i = 0; i < (entry.maxWidth - width); ++i) {
                sb.append(" ");
            }
        }
    }

    private void updateMaxWidth(OccurenceCounterEntry entry, long difference) {
        int width = Long.toString(difference).length();
        if (entry.maxWidth < width) {
            entry.maxWidth = width;
        }
    }

    private boolean isCategoryPermitted(OccurenceCounterEntry counter) {
        return counter.logger.isInfoEnabled();
    }

    public void setKeepRunning(boolean state) {
        keepRunning = state;
    }

    public void setSleepInterval(int interval) {
        countersDumpInterval = interval;
    }

    public HashSet<OccurenceCounterEntry> getCounters() {
        return (HashSet<OccurenceCounterEntry>) counters.clone();
    }
}