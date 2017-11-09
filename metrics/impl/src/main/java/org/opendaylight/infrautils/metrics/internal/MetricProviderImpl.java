/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static org.apache.logging.log4j.Level.ALL;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.Timer;
import com.codahale.metrics.log4j2.InstrumentedAppender;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.opendaylight.infrautils.metrics.MetricProvider;

/**
 * Implementation of {@link MetricProvider}.
 *
 * @author Michael Vorburger.ch
 */
public class MetricProviderImpl implements MetricProvider {

    private final MetricRegistry registry;
    private final JmxReporter jmxReporter;

    public MetricProviderImpl() {
        this.registry = new MetricRegistry();

        // TODO setUpJvmMetrics
        // TODO ThreadDeadlockHealthCheck.. but are healthchecks exposed via reporters?

        jmxReporter = setUpJmxReporter(registry);
        // TODO setUpSlf4jReporter

        // TODO test if this really works in Karaf through PAX Logging..
        instrumentLog4jV2(registry);
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

    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE") // null Filter and PatternLayout is cool with Log4j, so shut up
    private static void instrumentLog4jV2(MetricRegistry registry) {
        // TODO Confirm that Level ALL is a good idea?
        Level level = ALL;

        // http://metrics.dropwizard.io/3.1.0/manual/log4j/
        Filter filter = null;        // That's fine if we don't use filters; https://logging.apache.org/log4j/2.x/manual/filters.html
        PatternLayout layout = null; // The layout isn't used in InstrumentedAppender

        InstrumentedAppender appender = new InstrumentedAppender(registry, filter, layout, false);
        appender.start();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, level, filter);
        context.updateLoggers(config);
    }

    @Override
    public Meter newMeter(Object anchor, String id) {
        // TODO check if id has already been used
        return registry.meter(id);
    }

    @Override
    public Counter newCounter(Object anchor, String id) {
        // TODO check if id has already been used
        return registry.counter(id);
    }

    @Override
    public Histogram newHistogram(Object anchor, String id) {
        // TODO check if id has already been used
        return registry.histogram(id);
    }

    @Override
    public Timer newTimer(Object anchor, String id) {
        // TODO check if id has already been used
        return registry.timer(id);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Gauge newGauge(Object anchor, String id, MetricSupplier<Gauge> supplier) {
        // TODO check if id has already been used
        return registry.gauge(id, supplier);
    }

}
