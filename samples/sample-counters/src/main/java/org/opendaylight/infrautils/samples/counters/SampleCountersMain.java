package org.opendaylight.infrautils.samples.counters;

import org.opendaylight.infrautils.counters.impl.OccurenceCounter;

public class SampleCountersMain {

    public void init() {
        // Running all the example code in a different thread so the user will
        // be able to shutdown karaf. Otherwise, the blueprint container will
        // still be "stuck" in the init() method.
        new Thread(new TestCode()).start();
    }

    private class TestCode implements Runnable {

        @Override
        public void run() {
            System.out.println("\nStarted Counters Client Sample!");
            System.out.println("Change the writelog configuration to true at: org.opendaylight.counters.cfg");
            System.out.println("You will be able to see in the log, prints of the counters values.");
            SampleCounters.initial_counter.inc();
            for (int i = 0; i < 1000; ++i) {
                for (int j = 0; j < 1000; ++j) {
                    SampleCounters.high_frequency_event.inc();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                SampleCounters.low_frequency_event.inc();
            }
            System.out.println("Finished Counters Client!");
        }
    }

    enum SampleCounters {
        initial_counter, //
        low_frequency_event, //
        high_frequency_event;

        private OccurenceCounter counter;

        private SampleCounters() {
            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), name());
        }

        public void inc() {
            counter.inc();
        }
    }
}
