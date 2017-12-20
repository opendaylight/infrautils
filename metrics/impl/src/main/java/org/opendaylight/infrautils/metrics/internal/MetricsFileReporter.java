/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
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
    private static final Integer METRICS_INTERVAL = Integer.getInteger("metrics.interval.in.sec", 120);

    private final MetricRegistry registry;
    private final File parentDirectory;

    public MetricsFileReporter(MetricRegistry registry) {
        super(registry, "file-reporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.SECONDS);
        this.registry = registry;
        this.parentDirectory = new File(DATA_DIRECTORY, COUNTERS_DIRECTORY);
        startReporter();
    }

    private void startReporter() {
        start(METRICS_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {
            Calendar calendar = Calendar.getInstance();
            int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);
            int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);
            //retains one week worth of counters
            rotateLastWeekFile(dayOfTheWeek, hourOfTheDay);
            boolean append = true;
            File file = createFile(dayOfTheWeek, hourOfTheDay);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, append),
                    DEFAULT_ENCODING));
            pw.print("date,");
            pw.print(new Date());
            pw.println();

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                pw.print(entry.getKey());
                pw.print(",");
                pw.print(entry.getValue().getCount());
                pw.println();
            }
            pw.close();
        } catch (IOException e) {
            LOG.error("Failed to dump the counters {}", e.getMessage());
        }
    }

    private static String getFileNameName(int dayOfTheWeek, int hourOfTheDay) {
        return COUNTER_FILE_PREFIX + dayOfTheWeek + "." + hourOfTheDay;
    }

    private File createFile(int dayOfTheWeek, int hourOfTheDay) throws IOException {
        if (!parentDirectory.exists()) {
            LOG.info("Directory does not exist creating one {}", parentDirectory.getName());
            if (!parentDirectory.mkdirs()) {
                throw new RuntimeException("Failed to make directories");
            }
        }
        File file = new File(parentDirectory, getFileNameName(dayOfTheWeek, hourOfTheDay));
        if (!file.exists()) {
            LOG.info("File does not exist creating one {}", file.getName());
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create a file");
            }
        }
        return file;
    }

    private void rotateLastWeekFile(int dayOfTheWeek, int hourOfTheDay) throws IOException {
        int nextHour = hourOfTheDay < 23 ? hourOfTheDay + 1 : 0;
        File nextHourFile = new File(parentDirectory , getFileNameName(dayOfTheWeek, nextHour));
        if (nextHourFile.exists()) {
            boolean append = false;
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nextHourFile, append),
                    DEFAULT_ENCODING));
            pw.write(new Date().toString());
            pw.close();
        }
    }
}
