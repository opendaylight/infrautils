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
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricProvider;
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
    public void testMetricProviderImpl() {
        Meter meter1 = metrics.newMeter(this, "test.meter1");
        meter1.mark();
        meter1.mark(2);
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
        assertThrows(IllegalStateException.class, () -> meter1.mark());
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
    public void testTimeCheckedCallableNOK() {
        assertThrows(FileNotFoundException.class, () -> {
            metrics.newTimer(this, "test.timer").time(() -> {
                throw new FileNotFoundException();
            });
        });
    }

    @Test
    public void testTimeCallableNOK() {
        assertThrows(ArithmeticException.class, () -> {
            metrics.newTimer(this, "test.timer").time(() -> {
                throw new ArithmeticException();
            });
        });
    }

    @Test
    public void testTimeCheckedRunnableNOK() {
        assertThrows(FileNotFoundException.class, () -> {
            metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() throws FileNotFoundException {
                    throw new FileNotFoundException();
                }

                // This unused method is required so that IDEs like Eclipse do not turn this into a lambda;
                // because if it is a lambda, then it will invoked the variant of time() which takes a
                // CheckedCallable instead of the CheckedRunnable one we want to test here.
                @SuppressWarnings("unused")
                private void foo() { }
            });
        });
    }

    @Test
    public void testTimeRunnableNOK() {
        assertThrows(ArithmeticException.class, () -> {
            metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() throws FileNotFoundException {
                    throw new ArithmeticException();
                }

                @SuppressWarnings("unused")
                private void foo() { }
            });
        });
    }

    @Test
    public void testDupeMeterID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            metrics.newMeter(this, "test.meter1");
        });
    }

    @Test
    public void testDupeAnyID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            // NB: We cannot register a Counter (not a Meter) with the same ID, either
            metrics.newCounter(this, "test.meter1");
        });
    }

    // TODO testCloseMeter() - newMeter, close it, same ID should work again

    // TODO testBadID .. startsWith("odl") no spaces only dots
    // TODO             also enforce all lower case except String after last dot (before is package)
    // TODO             also enforce only 4 parts? instead of String id have String project, String "bundle" (feature) ?

    // TODO testReadJMX() using org.opendaylight.infrautils.utils.management.MBeanUtil from https://git.opendaylight.org/gerrit/#/c/65153/

}
