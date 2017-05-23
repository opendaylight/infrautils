/*
 * Copyright (c) 2016, 2017 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.counters.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.infrautils.counters.impl.service.CountersDumperThread;
import org.opendaylight.infrautils.counters.impl.service.OccurenceCounterEntry;
import org.opendaylight.infrautils.utils.TablePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountersMain {

    protected static final Logger LOG = LoggerFactory.getLogger(CountersMain.class);
    private CountersDumperThread countersRunnable = null;
    private Thread countersThread = null;
    private volatile int interval = 0;
    private boolean initialized = false;
    private volatile boolean shouldWrite = false;

    public void initialize() {
        initialized = true;
        if (shouldWrite) {
            startDumpersThread();
        }
    }

    private void startDumpersThread() {
        countersRunnable = new CountersDumperThread(interval);
        countersThread = new Thread(countersRunnable);
        countersThread.setName("CountersThread");
        countersThread.start();
        LOG.info("Counters thread started");
    }

    public void setWritelog(boolean shouldWrite) {
        // Change from false to true, and not init time
        if (!this.shouldWrite && shouldWrite && initialized) {
            startDumpersThread();
            // Change from true to false
        } else if (this.shouldWrite && !shouldWrite) {
            stopTheCountersThread();
        }
        this.shouldWrite = shouldWrite;
    }

    public void setInterval(int interval) {
        if (this.interval != interval) {
            this.interval = interval;
            if (countersRunnable != null) { // Runtime update
                LOG.info("Counters interval updated in runtime to {}", interval);
                countersRunnable.setSleepInterval(interval);
                countersThread.interrupt();
            }
        }
    }

    public void clean() {
        LOG.info("Counters Thread Clean called!");
        stopTheCountersThread();
    }

    private void stopTheCountersThread() {
        if (countersRunnable != null) {
            countersRunnable.setKeepRunning(false);
            countersThread.interrupt();
            try {
                countersThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void clearAllCounters(String[] filterGroupNames, String[] filterCounterNames) {
        countersRunnable.clearAllCounters(filterGroupNames, filterCounterNames);
    }

    public String dumpCounters(String regex) {
        if (regex == null) {
            regex = "";
        }
        Pattern pattern = Pattern.compile(regex);
        int sortByColumn = 0;
        TablePrinter printer = new TablePrinter(sortByColumn);
        printer.setColumnNames("Counter name", "Value");

        for (OccurenceCounterEntry entry : countersRunnable.getCounters()) {
            String counterName = getCounterFullName(entry.counter.group, entry.counter.name);
            Matcher matcher = pattern.matcher(counterName);
            if (matcher.find()) {
                printer.addRow(counterName, Long.toString(entry.counter.get()));
            }
        }

        return printer.toString();
    }

    private String getCounterFullName(String group, String counter) {
        return String.format("%s::%s", group, counter);
    }
}
