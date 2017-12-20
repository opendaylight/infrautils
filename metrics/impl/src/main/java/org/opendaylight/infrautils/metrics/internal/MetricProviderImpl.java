/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static java.util.Objects.requireNonNull;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.Timer;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;

/**
 * Implementation of {@link MetricProvider}.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = MetricProvider.class)
public class MetricProviderImpl implements MetricProvider {

    private final MetricRegistry registry;
    private final JmxReporter jmxReporter;

    public MetricProviderImpl() {
        this.registry = new MetricRegistry();

        // TODO setUpJvmMetrics
        // TODO ThreadDeadlockHealthCheck.. but are healthchecks exposed via reporters?

        jmxReporter = setUpJmxReporter(registry);
        // TODO setUpSlf4jReporter

        // TODO really get this to work in Karaf, through PAX Logging.. (it's currently NOK)
        // instrumentLog4jV2(registry);
    }

    @PreDestroy
    public void close() {
        jmxReporter.close();
    }

    private static JmxReporter setUpJmxReporter(MetricRegistry registry) {
        JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();
        return reporter;
    }

    // http://metrics.dropwizard.io/3.1.0/manual/log4j/
//    private static void instrumentLog4jV2(MetricRegistry registry) {
//        // TODO Confirm that Level ALL is a good idea?
//        Level level = ALL;
//
//        InstrumentedAppender appender = new InstrumentedAppender(registry,
//                null /* null Filter fine, because we don't use filters */,
//                null /* null PatternLayout, because the layout isn't used in InstrumentedAppender */, false);
//        appender.start();
//
//        LoggerContext context = (LoggerContext) LogManager.getContext(false);
//        Configuration config = context.getConfiguration();
//        config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, level,
//                null /* null Filter fine, because we don't use filters */);
//        context.updateLoggers(config);
//    }

    @Override
    public Meter newMeter(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        return registry.meter(id);
    }

    @Override
    public Counter newCounter(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        return registry.counter(id);
    }

    @Override
    public Histogram newHistogram(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        return registry.histogram(id);
    }

    @Override
    public Timer newTimer(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        return registry.timer(id);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Gauge newGauge(Object anchor, String id, MetricSupplier<Gauge> supplier) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        return registry.gauge(id, supplier);
    }

    private void checkID(String id) {
        requireNonNull(id, "id == null");
        if (registry.getNames().contains(id)) {
            throw new IllegalArgumentException("Metric ID already used: " + id);
        }
    }

}
