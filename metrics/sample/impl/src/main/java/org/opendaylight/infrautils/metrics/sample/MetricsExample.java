/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.sample;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.opendaylight.infrautils.utils.concurrent.JdkFutures.addErrorLogging;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.Counter;
import org.opendaylight.infrautils.metrics.Labeled;
import org.opendaylight.infrautils.metrics.Meter;
import org.opendaylight.infrautils.metrics.MetricDescriptor;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.utils.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example illustrating usage of metrics API, and demo.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class MetricsExample implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsExample.class);

    private final Counter counterWithoutLabel;
    private final Meter meterWithoutLabel;
    private final Meter meterWithOneFixedLabel;
    private final Meter meterWithTwoFixedLabels;
    private final Labeled<Meter> meterWithOneDynamicLabel;
    private final Labeled<Labeled<Meter>> meterWithTwoDynamicLabels;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor("cron", LOG);
    private final Random random = new Random();

    @Inject
    public MetricsExample(MetricProvider metricProvider) {
        counterWithoutLabel = metricProvider.newCounter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_counter_without_labels")
                .description("Example counter metric without any labels").build());

        meterWithoutLabel = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter_without_labels")
                .description("Example meter metric without any labels").build());

        meterWithOneFixedLabel = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter_1_label")
                .description("Example meter metric with 1 label and a fixed label value").build(),
                "port").label("123");

        meterWithTwoFixedLabels = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter_2_labels")
                .description("Example meter metric with 2 labels and fixed label values").build(),
                "port", "mac").label("123").label("6C:0D:E6:67:7E:68");

        meterWithOneDynamicLabel = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter_1_dynlabel")
                .description("Example meter metric with 1 label and label value set in using code").build(),
                "port");

        meterWithTwoDynamicLabels = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("example_meter_2_dynlabels")
                .description("Example meter metric with 2 labels and its label values set in using code").build(),
                "port", "mac");
    }

    @PostConstruct
    public void init() {
        addErrorLogging(executor.scheduleWithFixedDelay(this, 0, 500, MILLISECONDS), LOG, "schedule interrupted");
    }

    @PreDestroy
    public void close() {
        counterWithoutLabel.close();
        meterWithoutLabel.close();
        meterWithOneFixedLabel.close();
        meterWithTwoFixedLabels.close();
        // TODO meterWithOneDynamicLabel.close() how to?
        // TODO meterWithTwoDynamicLabels.close() how to?

        executor.shutdownNow();
    }

    @Override
    public void run() {
        counterWithoutLabel.increment(random.nextInt(200) - 100);

        meterWithoutLabel.mark(random.nextInt(100));
        meterWithOneFixedLabel.mark(random.nextInt(100));
        meterWithTwoFixedLabels.mark(random.nextInt(100));

        meterWithOneDynamicLabel.label(/* port */ "456").mark(random.nextInt(100));
        meterWithTwoDynamicLabels
            .label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68")
            .mark(random.nextInt(100));

        // see the MetricsAdvancedExample for how to do meter.port(456).mac("1A:0B:F2:25:1C:68").mark();
    }

}
