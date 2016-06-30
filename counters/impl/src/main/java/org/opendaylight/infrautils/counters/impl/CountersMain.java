/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
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

    protected static final Logger logger = LoggerFactory.getLogger(CountersMain.class);
    private CountersDumperThread countersRunnable = null;
    private Thread countersThread = null;
    private volatile int interval = 0;
    private boolean initialized = false;
    private volatile boolean shouldWrite = false;

    public void initialize() {
        try {
            initialized = true;
            if (shouldWrite) {
                startDumpersThread();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void startDumpersThread() throws Exception {
        countersRunnable = new CountersDumperThread(interval);
        countersThread = new Thread(countersRunnable);
        countersThread.start();
        logger.info("Counters thread started");
    }

    public void setWritelog(boolean shouldWrite) {
        try {
            if (!this.shouldWrite && shouldWrite && initialized) { // Change
                                                                   // from false
                                                                   // to true,
                                                                   // and not
                                                                   // init time
                startDumpersThread();
            } else if (this.shouldWrite && !shouldWrite) { // Change from true
                                                           // to false
                stopTheCountersThread();
            }
            this.shouldWrite = shouldWrite;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setInterval(int interval) {
        if (this.interval != interval) {
            this.interval = interval;
            if (countersRunnable != null) { // Runtime update
                logger.info("Counters interval updated in runtime to: " + interval);
                countersRunnable.setSleepInterval(interval);
                countersThread.interrupt();
            }
        }
    }

    public void clean() {
        logger.info("Counters Thread Clean called!");
        stopTheCountersThread();
    }

    private void stopTheCountersThread() {
        if (countersRunnable != null) {
            countersRunnable.setKeepRunning(false);
            countersThread.interrupt();
            try {
                countersThread.join();
            } catch (InterruptedException e) {
            }
            countersThread = null;
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
        String counterName = String.format("%s::%s", group, counter);
        return counterName;
    }
}
