/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static com.codahale.metrics.Slf4jReporter.LoggingLevel.INFO;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import java.lang.management.ManagementFactory;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MetricProvider}.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@OsgiServiceProvider(classes = MetricProvider.class)
public class MetricProviderImpl implements MetricProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MetricProviderImpl.class);

    private final MetricRegistry registry;
    private final ThreadsWatcher threadsWatcher;
    private final JmxReporter jmxReporter;
    private final Slf4jReporter slf4jReporter;

    public MetricProviderImpl() {
        this.registry = new MetricRegistry();

        setUpJvmMetrics(registry);
        threadsWatcher = new ThreadsWatcher(1, MINUTES);

        jmxReporter = setUpJmxReporter(registry);
        slf4jReporter = setUpSlf4jReporter(registry);

        // TODO really get this to work in Karaf, through PAX Logging.. (it's currently NOK)
        // instrumentLog4jV2(registry);
    }

    @PreDestroy
    public void close() {
        jmxReporter.close();
        slf4jReporter.close();
        threadsWatcher.close();
    }

    private static void setUpJvmMetrics(MetricRegistry registry) {
        ThreadDeadlockDetector threadDeadlockDetector = new ThreadDeadlockDetector();
        FileDescriptorRatioGauge fileDescriptorRatioGauge = new FileDescriptorRatioGauge();

        registry.registerAll(new GarbageCollectorMetricSet());
        registry.registerAll(new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        registry.registerAll(new CachedThreadStatesGaugeSet(getThreadMXBean(), threadDeadlockDetector, 13, SECONDS));
        registry.registerAll(new MemoryUsageGaugeSet());
        registry.registerAll(new ClassLoadingGaugeSet());
        registry.gauge("odl.infrautils.FileDescriptorRatio", () -> fileDescriptorRatioGauge);
    }

    private static JmxReporter setUpJmxReporter(MetricRegistry registry) {
        JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();
        LOG.info("JmxReporter started, ODL application's metrics are now available via JMX");
        return reporter;
    }

    private static Slf4jReporter setUpSlf4jReporter(MetricRegistry registry) {
        Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(registry)
                .convertDurationsTo(MILLISECONDS)
                .convertRatesTo(SECONDS)
                .outputTo(LOG)
                .prefixedWith("JVM")
                .withLoggingLevel(INFO)
                .shutdownExecutorOnStop(true)
                .build();
        // NB: We do intentionally *NOT* start() the Slf4jReporter to log all metrics regularly;
        // as that will spam the log, and we have our own file based reporting instead.
        // We do log system metrics once at boot up:
        LOG.info("One time system JVM metrics FYI; "
                + "to watch continously, monitor via JMX or enable periodic file dump option");
        slf4jReporter.report();
        return slf4jReporter;
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
