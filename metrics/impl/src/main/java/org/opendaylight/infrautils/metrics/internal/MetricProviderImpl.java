/*
 * Copyright (c) 2017 - 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static java.util.Objects.requireNonNull;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.infrautils.metrics.CloseableMetric;
import org.opendaylight.infrautils.metrics.MetricProvider;
import org.opendaylight.infrautils.utils.function.CheckedCallable;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;
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
    private final JmxReporter jmxReporter;
    private final MetricsFileReporter fileReporter;

    public MetricProviderImpl() {
        this.registry = new MetricRegistry();

        // TODO setUpJvmMetrics
        // TODO ThreadDeadlockHealthCheck.. but are healthchecks exposed via reporters?

        jmxReporter = setUpJmxReporter(registry);

        fileReporter = new MetricsFileReporter(registry);
        fileReporter.startReporter(); //TODO make it optional
        // TODO setUpSlf4jReporter

        // TODO really get this to work in Karaf, through PAX Logging.. (it's currently NOK)
        // instrumentLog4jV2(registry);
    }

    @PreDestroy
    public void close() {
        jmxReporter.close();
        fileReporter.close();
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
    public org.opendaylight.infrautils.metrics.Meter newMeter(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        com.codahale.metrics.Meter meter = registry.meter(id);
        return new MeterImpl(id) {

            @Override
            public void mark() {
                checkIfClosed();
                meter.mark();
            }

            @Override
            public void mark(long howMany) {
                checkIfClosed();
                meter.mark(howMany);
            }

            @Override
            public long get() {
                return meter.getCount();
            }
        };
    }

    @Override
    public org.opendaylight.infrautils.metrics.Counter newCounter(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        com.codahale.metrics.Counter counter = registry.counter(id);
        return new CounterImpl(id) {

            @Override
            public void increment() {
                checkIfClosed();
                counter.inc();
            }

            @Override
            public void increment(long howMany) {
                checkIfClosed();
                counter.inc(howMany);
            }

            @Override
            public void decrement() {
                checkIfClosed();
                counter.dec();
            }

            @Override
            public void decrement(long howMany) {
                checkIfClosed();
                counter.dec(howMany);
            }

            @Override
            public long get() {
                return counter.getCount();
            }
        };
    }

    @Override
    public org.opendaylight.infrautils.metrics.Timer newTimer(Object anchor, String id) {
        requireNonNull(anchor, "anchor == null");
        checkID(id);
        com.codahale.metrics.Timer timer = registry.timer(id);
        return new TimerImpl(id) {

            @Override
            @SuppressWarnings({ "checkstyle:IllegalCatch", "unchecked" })
            public <T, E extends Exception> T time(CheckedCallable<T, E> event) throws E {
                checkIfClosed();
                try {
                    return timer.time(() -> event.call());
                } catch (Exception e) {
                    throw (E) e;
                }
            }

            @Override
            @SuppressWarnings({ "checkstyle:IllegalCatch", "checkstyle:AvoidHidingCauseException", "unchecked" })
            @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE") // getCause() will be Exception not Throwable
            public <E extends Exception> void time(CheckedRunnable<E> event) throws E {
                checkIfClosed();
                try {
                    timer.time(() -> {
                        try {
                            event.run();
                        } catch (Exception exception) {
                            throw new InternalRuntimeException(exception);
                        }
                    });
                } catch (InternalRuntimeException e) {
                    throw (E) e.getCause();
                }
            }
        };
    }

    private void checkID(String id) {
        requireNonNull(id, "id == null");
        if (registry.getNames().contains(id)) {
            throw new IllegalArgumentException("Metric ID already used: " + id);
        }
    }

    private void remove(String id) {
        if (!registry.remove(id)) {
            LOG.warn("Metric remove did not actualy remove: {}", id);
        }
    }

    private abstract class CloseableMetricImpl implements CloseableMetric {
        private volatile boolean isClosed = false;
        private final String id;

        CloseableMetricImpl(String id) {
            this.id = id;
        }

        protected void checkIfClosed() {
            if (isClosed) {
                throw new IllegalStateException("Meter closed: " + id);
            }
        }

        @Override
        public void close() {
            isClosed = true;
            remove(id);
        }
    }

    private abstract class MeterImpl extends CloseableMetricImpl implements org.opendaylight.infrautils.metrics.Meter {
        MeterImpl(String id) {
            super(id);
        }
    }

    private abstract class CounterImpl extends CloseableMetricImpl
            implements org.opendaylight.infrautils.metrics.Counter {
        CounterImpl(String id) {
            super(id);
        }
    }

    private abstract class TimerImpl extends CloseableMetricImpl implements org.opendaylight.infrautils.metrics.Timer {
        TimerImpl(String id) {
            super(id);
        }
    }

    private static class InternalRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InternalRuntimeException(Exception exception) {
            super(exception);
        }
    }
}
