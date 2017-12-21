/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import static java.lang.management.ManagementFactory.getThreadMXBean;
import static org.opendaylight.infrautils.utils.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.opendaylight.infrautils.utils.concurrent.JdkFutures.addErrorLogging;

import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatic JVM thread limit and deadlock detection logging.
 *
 * @author Michael Vorburger.ch
 */
class ThreadsWatcher implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadsWatcher.class);

    private final int maxThreads;
    private final ScheduledExecutorService scheduledExecutor;
    private final ThreadDeadlockDetector threadDeadlockDetector = new ThreadDeadlockDetector();
    private final ThreadDump threadDump = new ThreadDump(getThreadMXBean());

    ThreadsWatcher(int maxThreads, int interval, TimeUnit timeUnit) {
        this.maxThreads = maxThreads;
        scheduledExecutor = newSingleThreadScheduledExecutor("infrautils.metrics.ThreadsWatcher", LOG);
        addErrorLogging(scheduledExecutor.scheduleAtFixedRate(this, 0, interval, timeUnit), LOG, "scheduleAtFixedRate");
    }

    void close() {
        scheduledExecutor.shutdown();
    }

    @Override
    public void run() {
        int currentNumberOfThreads = getThreadMXBean().getThreadCount();
        Set<String> deadlockedThreadsStackTrace = threadDeadlockDetector.getDeadlockedThreads();
        if (!deadlockedThreadsStackTrace.isEmpty()) {
            LOG.error("Oh nose - there are {} deadlocked threads!! :-(", deadlockedThreadsStackTrace.size());
            for (String deadlockedThreadStackTrace : deadlockedThreadsStackTrace) {
                LOG.error("Deadlocked thread stack trace: {}", deadlockedThreadStackTrace);
            }
            logAllThreads();

        } else if (currentNumberOfThreads >= maxThreads) {
            LOG.warn("Oh nose - there are now {} threads, more than maximum threshold {}! "
                    + "(totalStarted: {}, peak: {}, daemons: {})",
                    currentNumberOfThreads, maxThreads, getThreadMXBean().getTotalStartedThreadCount(),
                    getThreadMXBean().getPeakThreadCount(), getThreadMXBean().getDaemonThreadCount());
            logAllThreads();
        }
    }

    @VisibleForTesting
    void logAllThreads() {
        try (OutputStream loggingOutputStream = new LoggingOutputStream()) {
            threadDump.dump(loggingOutputStream);
        } catch (IOException e) {
            LOG.error("LoggingOutputStream.close() failed", e);
        }
    }

    private static class LoggingOutputStream extends ByteArrayOutputStream {

        @Override
        public void close() throws IOException {
            String lines = this.toString("UTF-8"); // UTF-8 because that is what ThreadDump writes it in
            LOG.warn("Thread Dump:\n{}", lines);
        }
    }
}
