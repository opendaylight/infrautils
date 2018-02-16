/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.opendaylight.infrautils.testutils.Asserts.assertThrows;

import com.google.errorprone.annotations.Var;
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.metrics.Timer;
import org.opendaylight.infrautils.metrics.internal.MetricProviderImpl;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;

/**
 * Unit Test for MetricProviderImpl.
 *
 * @author Michael Vorburger.ch
 */
public class MetricProviderTest {

    public @Rule LogRule logRule = new LogRule();

    public @Rule LogCaptureRule logCaptureRule = new LogCaptureRule();

    private final MetricProvider metrics = new MetricProviderImpl();

    @After
    public void afterEachTest() {
        ((MetricProviderImpl) metrics).close();
    }

    @Test
    public void testMeter() {
        Meter meter1 = metrics.newMeter(MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics")
                .id("test_meter1").build());
        meter1.mark();
        meter1.mark(2);
        assertThat(meter1.get()).isEqualTo(3);
    }

    @Test
    public void testMeterWith2Labels() {
        Labeled<Labeled<Meter>> meterWithTwoLabels = metrics.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(),
                "port", "mac");
        Meter meterA = meterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        meterA.mark(3);
        assertThat(meterA.get()).isEqualTo(3);

        Meter meterB = meterWithTwoLabels.label(/* port */ "789").label(/* MAC */ "1A:0B:F2:25:1C:68");
        meterB.mark(1);
        assertThat(meterB.get()).isEqualTo(1);
        assertThat(meterA.get()).isEqualTo(3);

        Meter againMeterA = meterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        assertThat(againMeterA.get()).isEqualTo(3);
    }

    @Test
    public void testCounterWith2Labels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter1").build(),
                "port", "mac");
        Counter counterA = counterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        counterA.increment(3);
        assertThat(counterA.get()).isEqualTo(3);

        Counter counterB = counterWithTwoLabels.label(/* port */ "789").label(/* MAC */ "1A:0B:F2:25:1C:68");
        counterB.increment(1);
        assertThat(counterB.get()).isEqualTo(1);
        assertThat(counterA.get()).isEqualTo(3);

        Counter againCounterA = counterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        assertThat(againCounterA.get()).isEqualTo(3);
    }

    @Test
    public void testCounterWith5Labels() {
        Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> counterWithFiveLabels =
                metrics.newCounter(MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics")
                                .id("test_counter2").build(), "label1", "label2",
                                 "label3", "label4", "label5");
        Counter counterA = counterWithFiveLabels.label("l1").label("l2").label("l3")
                .label("l4").label("l5");
        counterA.increment(5);
        assertThat(counterA.get()).isEqualTo(5);

        Counter againCounterA = counterWithFiveLabels.label("l1").label("l2").label("l3")
                .label("l4").label("l5");
        assertThat(againCounterA.get()).isEqualTo(5);
    }

    @Test
    public void testCounterOperationsWithLabels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_opers").build(),
                "l1", "l2");
        Counter counterA = counterWithTwoLabels.label("l1value").label("l2value");
        counterA.increment();
        counterA.increment();
        assertThat(counterA.get()).isEqualTo(2);

        counterA.decrement();
        assertThat(counterA.get()).isEqualTo(1);

        counterA.increment(5);
        assertThat(counterA.get()).isEqualTo(6);
        counterA.decrement(2);
        assertThat(counterA.get()).isEqualTo(4);

        counterA.close();
    }

    @Test
    public void testSameCounterUpdateOperationsWithLabels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_upd_opers").build(),
                "l1", "l2");
        Counter counterA = counterWithTwoLabels.label("l1value").label("l2value");
        counterA.increment(51);
        assertThat(counterA.get()).isEqualTo(51);

        counterA.decrement();
        assertThat(counterA.get()).isEqualTo(50);

        Labeled<Labeled<Counter>> sameCounterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_upd_opers").build(),
                "l1", "l2");
        Counter sameCounterA = sameCounterWithTwoLabels.label("l1value").label("l2value");

        assertThat(sameCounterA).isEqualTo(counterA);
        assertThat(sameCounterA.get()).isEqualTo(50);
        sameCounterA.increment(5);
        assertThat(sameCounterA.get()).isEqualTo(55);

        sameCounterA.decrement(10);
        assertThat(sameCounterA.get()).isEqualTo(45);

        sameCounterA.close();
    }

    @Test
    public void testCloseMeterAndCreateNewOneWithSameID() {
        Meter meter = metrics.newMeter(this, "test.meter");
        meter.close();
        Meter meterAgain = metrics.newMeter(this, "test.meter");
        meterAgain.mark();
    }

    @Test
    public void testUseClosedMeter() {
        Meter meter1 = metrics.newMeter(this, "test.meter1");
        meter1.close();
        assertThrows(IllegalStateException.class, meter1::mark);
        // Closing an already closed metric throws an IllegalStateException
        assertThrows(IllegalStateException.class, meter1::close);
    }

    @Test
    public void testUseClosedLabeledMeter() {
        Labeled<Meter> meterWithLabel = metrics.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "label1");
        meterWithLabel.label("label1value").mark();
        assertThat(meterWithLabel.label("label1value").get()).isEqualTo(1);
        meterWithLabel.label("label1value").close();
        // NOT assertThrows(IllegalStateException.class, () -> meterWithLabel.label("label1value").mark());
        // because we can recreate a metric with the same label, but it will be a new one:
        meterWithLabel.label("label1value").mark();
        assertThat(meterWithLabel.label("label1value").get()).isEqualTo(1);
    }

    @Test
    public void testUseClosedLabeledCounter() {
        Labeled<Counter> counterWithLabel = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "label1");
        counterWithLabel.label("label1value").increment();
        assertThat(counterWithLabel.label("label1value").get()).isEqualTo(1);
        counterWithLabel.label("label1value").close();
        // use counter after close operation
        counterWithLabel.label("label1value").increment();
        assertThat(counterWithLabel.label("label1value").get()).isEqualTo(1);
    }

    @Test
    public void testTimeRunnableOK() {
        metrics.newTimer(this, "test.timer").time(() -> {
            for (@SuppressWarnings("unused") int sum = 0, i = 1; i < 101; i++) {
                sum += i;
            }
        });
    }

    @Test
    public void testTimeCallableOK() {
        assertThat(metrics.newTimer(this, "test.timer").time(() -> {
            @Var int sum = 0;
            for (int i = 1; i < 101; i++) {
                sum += i;
            }
            return sum;
        })).isEqualTo(5050);
    }

    @Test
    public void testTimeRunnableOKWithLabels() {
        Labeled<Labeled<Timer>> timerWithTwoLabels = metrics.newTimer(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_timer_with_labels").build(),
                "l1", "l2");
        Timer timerA = timerWithTwoLabels.label("l1value").label("l2value");
        timerA.time(() -> {
            for (@SuppressWarnings("unused") int sum = 0, i = 1; i < 101; i++) {
                sum += i;
            }
        });
    }

    @Test
    public void testTimeCallableWithLabels() {
        Labeled<Labeled<Timer>> timerWithTwoLabels = metrics.newTimer(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_timer_with_labels").build(),
                "l1", "l2");
        Timer timerA = timerWithTwoLabels.label("l1value").label("l2value");
        assertThat(timerA.time(() -> {
            @Var int sum = 0;
            for (int i = 1; i < 101; i++) {
                sum += i;
            }
            return sum;
        })).isEqualTo(5050);
    }

    @Test
    public void testTimeCheckedCallableNOK() {
        assertThrows(FileNotFoundException.class, () -> metrics.newTimer(this, "test.timer").time(() -> {
            throw new FileNotFoundException();
        }));
    }

    @Test
    public void testTimeCallableNOK() {
        assertThrows(ArithmeticException.class, () -> metrics.newTimer(this, "test.timer").time(() -> {
            throw new ArithmeticException();
        }));
    }

    @Test
    public void testTimeCheckedRunnableNOK() {
        assertThrows(FileNotFoundException.class,
            () -> metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() throws FileNotFoundException {
                    throw new FileNotFoundException();
                }

                // This unused method is required so that IDEs like Eclipse do not turn this into a lambda;
                // because if it is a lambda, then it will invoked the variant of time() which takes a
                // CheckedCallable instead of the CheckedRunnable one we want to test here.
                @SuppressWarnings("unused")
                private void foo() {
                }
            }));
    }

    @Test
    public void testTimeRunnableNOK() {
        assertThrows(ArithmeticException.class,
            () -> metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() {
                    throw new ArithmeticException();
                }

                @SuppressWarnings("unused")
                private void foo() {
                }
            }));
    }

    @Test
    public void testDupeMeterID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> metrics.newMeter(this, "test.meter1"));
    }

    @Test
    public void testDupeAnyID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            // NB: We cannot register a Counter (not a Meter) with the same ID, either
            metrics.newCounter(this, "test.meter1");
        });
    }

    // TODO testReadJMX() using org.opendaylight.infrautils.utils.management.MBeanUtil from https://git.opendaylight.org/gerrit/#/c/65153/

}
