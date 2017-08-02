/*
 * Copyright (c) 2016, 2017 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.samples.counters;

import org.opendaylight.infrautils.counters.api.OccurenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleCountersMain {
    private static final Logger LOG = LoggerFactory.getLogger(SampleCountersMain.class);

    public void init() {
        // Running all the example code in a different thread so the user will
        // be able to shutdown karaf. Otherwise, the blueprint container will
        // still be "stuck" in the init() method.
        new Thread(new TestCode()).start();
    }

    private class TestCode implements Runnable {

        @Override
        public void run() {
            LOG.info("Started Counters Client Sample!");
            LOG.info("Change the writelog configuration to true at: org.opendaylight.counters.cfg");
            LOG.info("You will be able to see in the log, prints of the counters values.");
            SampleCounters.INITIAL_COUNTER.inc();
            for (int i = 0; i < 1000; ++i) {
                for (int j = 0; j < 1000; ++j) {
                    SampleCounters.HIGH_FREQUENCY_EVENT.inc();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted", e);
                    return;
                }
                SampleCounters.LOW_FREQUENCY_EVENT.inc();
            }
            LOG.info("Finished Counters Client!");
        }
    }

    enum SampleCounters {
        INITIAL_COUNTER,
        LOW_FREQUENCY_EVENT,
        HIGH_FREQUENCY_EVENT;

        private final OccurenceCounter counter;

        SampleCounters() {
            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), name());
        }

        public void inc() {
            counter.inc();
        }
    }
}
