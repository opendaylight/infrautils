/*
 * Copyright © 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.tests;

import static org.junit.Assert.assertTrue;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import org.opendaylight.infrautils.metrics.internal.MetricsFileReporter;

public class MetricsFileReporterTest {
    @Test
    public void testReporter() throws IOException {
        MetricRegistry registry = new MetricRegistry();
        MetricsFileReporter fileReporter = new MetricsFileReporter(registry, Duration.ofSeconds(120));
        Counter counter = registry.counter("test.counter");
        counter.inc();
        Calendar calendar = Calendar.getInstance();
        int hourOfTheDayBeforeReport = calendar.get(Calendar.HOUR_OF_DAY);
        fileReporter.report(registry.getGauges(), registry.getCounters(),
                registry.getHistograms(), registry.getMeters(), registry.getTimers());
        Calendar calendar2 = Calendar.getInstance();
        int hourOfTheDayAfterReport = calendar2.get(Calendar.HOUR_OF_DAY);
        int dayOfTheWeek = calendar2.get(Calendar.DAY_OF_WEEK);
        if (hourOfTheDayAfterReport == hourOfTheDayBeforeReport) {
            //Make sure that we are looking at the right file so that junit test does not fail at the turn of hour
            File file = fileReporter.createFile(dayOfTheWeek, hourOfTheDayAfterReport);
            List<String> fileLines = Files.readLines(file, Charset.defaultCharset());
            assertTrue(fileLines.contains("test.counter,count,1,diff,1"));
            file.deleteOnExit();
        }
        fileReporter.close();
    }
}
