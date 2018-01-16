/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsFileReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsFileReporter.class);

    private static final String COUNTER_FILE_PREFIX = "counter.";
    private static final String DATA_DIRECTORY = "data";
    private static final String COUNTERS_DIRECTORY = "counters";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Integer METRICS_INTERVAL = 120; // TODO make it cfg variable
    private static final String SEPARATOR = ",";

    private final File parentDirectory;
    private final Map<String, Long> oldCounters = new HashMap<>();

    public MetricsFileReporter(MetricRegistry registry) {
        super(registry, "file-reporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.SECONDS);
        this.parentDirectory = new File(DATA_DIRECTORY, COUNTERS_DIRECTORY);
    }

    public void startReporter() {
        start(METRICS_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void report(@SuppressWarnings("rawtypes")
                       SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {
            Calendar calendar = Calendar.getInstance();
            int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);
            int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // retains one week worth of counters
            rotateLastWeekFile(dayOfTheWeek, hourOfTheDay);
            boolean append = true;
            File file = createFile(dayOfTheWeek, hourOfTheDay);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, append),
                    DEFAULT_ENCODING));
            pw.print("date,");
            pw.print(new Date());
            pw.println();

            pw.println("Counters:");
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                Counter newCounter = entry.getValue();
                // avoid unnecessary write to report file
                // report the counter only if there is a change
                Long oldCounterObj = oldCounters.get(entry.getKey());
                long oldCounter = oldCounterObj != null ? oldCounterObj.longValue() : 0;
                if (newCounter.getCount() != oldCounter) {
                    pw.print(entry.getKey());
                    printWithSeparator(pw, "count", entry.getValue().getCount());
                    printWithSeparator(pw, "diff",
                            entry.getValue().getCount() - oldCounter);
                    pw.println();
                }
            }
            pw.println("Gauges:");
            for (@SuppressWarnings("rawtypes") Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                pw.print(entry.getKey());
                pw.println(entry.getValue().getValue());
            }
            pw.println("Histograms:");
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                pw.print(entry.getKey());
                printWithSeparator(pw, "count", entry.getValue().getCount());
                printSampling(pw, entry.getValue());
                pw.println();
            }
            pw.println("Meters:");
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                pw.print(entry.getKey());
                printMeter(pw, entry.getValue());
            }
            pw.println("Timers:");
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                pw.print(entry.getKey());
                printSampling(pw, entry.getValue());
                printMeter(pw, entry.getValue());
            }
            pw.close();
        } catch (IOException e) {
            LOG.error("Failed to report counters to files", e);
        }
        counters.entrySet().forEach(entry -> oldCounters.put(entry.getKey(), entry.getValue().getCount()));
    }

    private static void printSampling(PrintWriter pw, Sampling sampling) {
        Snapshot snapshot = sampling.getSnapshot();
        printWithSeparator(pw, "min", snapshot.getMin());
        printWithSeparator(pw, "max", snapshot.getMax());
        printWithSeparator(pw, "mean", snapshot.getMean());
    }

    private static void printMeter(PrintWriter pw, Metered meter) {
        printWithSeparator(pw, "count", meter.getCount());
        printWithSeparator(pw, "oneMinuteRate", meter.getOneMinuteRate());
        printWithSeparator(pw, "fiveMinuteRate", meter.getFiveMinuteRate());
        printWithSeparator(pw, "fifteenMinuteRate", meter.getFifteenMinuteRate());
        pw.println();
    }

    private static void printWithSeparator(PrintWriter pw, String name, Object val) {
        printSeparator(pw);
        pw.print(name);
        printSeparator(pw);
        pw.print(val);
    }

    private static void printSeparator(PrintWriter pw) {
        pw.print(SEPARATOR);
    }

    private static String getFileName(int dayOfTheWeek, int hourOfTheDay) {
        return COUNTER_FILE_PREFIX + dayOfTheWeek + "." + hourOfTheDay;
    }

    public File createFile(int dayOfTheWeek, int hourOfTheDay) throws IOException {
        if (!parentDirectory.exists()) {
            LOG.info("Directory does not exist, creating it: {}", parentDirectory.getName());
            if (!parentDirectory.mkdirs()) {
                throw new IOException("Failed to make directories: " + parentDirectory.toString());
            }
        }
        File file = new File(parentDirectory, getFileName(dayOfTheWeek, hourOfTheDay));
        if (!file.exists()) {
            LOG.info("File does not exist, creating it: {}", file.getName());
            if (!file.createNewFile()) {
                throw new IOException("Failed to create file: " + file.toString());
            }
        }
        return file;
    }

    private void rotateLastWeekFile(int dayOfTheWeek, int hourOfTheDay) throws IOException {
        int nextHour = hourOfTheDay < 23 ? hourOfTheDay + 1 : 0;
        File nextHourFile = new File(parentDirectory , getFileName(dayOfTheWeek, nextHour));
        if (nextHourFile.exists()) {
            boolean append = false;
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nextHourFile, append),
                    DEFAULT_ENCODING));
            pw.write(new Date().toString());
            pw.close();
        }
    }
}
