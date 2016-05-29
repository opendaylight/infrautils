/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.counters.impl.service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.infrautils.utils.TablePrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CountersDumperService implements ICountersDumperService {

    private static final String COUNTER_INTERVAL_PROPERTY = "countersDumperInterval";
//    private static final int DEFAULT_COUNTER_INTERVAL = 60000;
    private static final int DEFAULT_COUNTER_INTERVAL = 1000;
    private CountersDumperThread countersThread = null;
    protected static final Logger logger = LoggerFactory.getLogger(CountersDumperService.class);

    public void init() {
        try {
            String intervalStr = System.getProperty(COUNTER_INTERVAL_PROPERTY, Integer.toString(DEFAULT_COUNTER_INTERVAL));

            int dumpInterval = DEFAULT_COUNTER_INTERVAL;
            try {
                dumpInterval = Integer.valueOf(intervalStr);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            countersThread = new CountersDumperThread(dumpInterval);
            new Thread(countersThread).start();
            logger.info("CountersDumperService init finished");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void destroy() {
        countersThread.setKeepRunning(false);
    }

    public void clearAllCounters(String[] filterGroupNames, String[] filterCounterNames) {
        countersThread.clearAllCounters(filterGroupNames, filterCounterNames);
    }

    @Override
    public String dumpCounters(String regex) {
        if (regex == null) {
            regex = "";
        }
        Pattern pattern = Pattern.compile(regex);
        int sortByColumn = 0;
        TablePrinter printer = new TablePrinter(sortByColumn);
        printer.setColumnNames("Counter name", "Value");

        for (OccurenceCounterEntry entry : countersThread.getCounters()) {
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
