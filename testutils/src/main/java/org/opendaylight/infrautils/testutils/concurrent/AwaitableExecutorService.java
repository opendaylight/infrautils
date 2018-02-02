/*
 * Copyright © 2017 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ForwardingExecutorService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Executable service wrapper allowing callers to await completion.
 */
@SuppressFBWarnings("JLM_JSR166_UTILCONCURRENT_MONITORENTER")
public class AwaitableExecutorService extends ForwardingExecutorService {
    private final ExecutorService delegate;
    private final AtomicLong pendingJobs = new AtomicLong(0);

    /**
     * Create a new wrapper for the given {@link ExecutorService}, adding the ability to wait for
     * job completion.
     *
     * @param delegate The executor service to wrap.
     */
    public AwaitableExecutorService(ExecutorService delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected ExecutorService delegate() {
        return delegate;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        pendingJobs.incrementAndGet();
        return delegate.submit(wrapCallable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        pendingJobs.incrementAndGet();
        return delegate.submit(wrapRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        pendingJobs.incrementAndGet();
        return delegate.submit(wrapRunnable(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        pendingJobs.addAndGet(tasks.size());
        return delegate.invokeAll(tasks.stream().map(this::wrapCallable).collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        pendingJobs.addAndGet(tasks.size());
        return delegate.invokeAll(tasks.stream().map(this::wrapCallable).collect(Collectors.toList()), timeout,
                unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        pendingJobs.addAndGet(tasks.size());
        return delegate.invokeAny(tasks.stream().map(this::wrapCallable).collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        pendingJobs.addAndGet(tasks.size());
        return delegate.invokeAny(tasks.stream().map(this::wrapCallable).collect(Collectors.toList()), timeout,
                unit);
    }

    @Override
    public void execute(Runnable command) {
        pendingJobs.incrementAndGet();
        delegate.execute(wrapRunnable(command));
    }

    /**
     * Wait for completion: this method will wait until all submitted jobs have completed, subject to the provided
     * timeout. This is inherently racy if external job submission is continuing; if the submitted jobs themselves
     * submit new jobs, this will wait for those newly-submitted jobs to complete too.
     *
     * @param timeout The maximum time to wait.
     * @param unit The unit used for the timeout.
     *
     * @return {@code true} if the submitted jobs have completed, {@code false} if they haven’t.
     */
    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long maxDuration = unit.toNanos(timeout);
        synchronized (pendingJobs) {
            while (System.nanoTime() - startTime < maxDuration && !isCompleted()) {
                long waitDuration = TimeUnit.NANOSECONDS.toMillis(maxDuration - (System.nanoTime() - startTime));
                if (waitDuration > 0) {
                    pendingJobs.wait(waitDuration);
                }
            }
        }
        return isCompleted();
    }

    /**
     * Indicates whether all submitted jobs have completed.
     *
     * @return {@code true} if all submitted jobs have completed, {@code false} otherwise.
     */
    public boolean isCompleted() {
        return pendingJobs.get() == 0L;
    }

    private <T> Callable<T> wrapCallable(Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } finally {
                decrementAndNotify();
            }
        };
    }

    private Runnable wrapRunnable(Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                decrementAndNotify();
            }
        };
    }

    /**
     * Decrements the counter of pending jobs, and notifies waiting threads if the count reaches 0.
     */
    private void decrementAndNotify() {
        synchronized (pendingJobs) {
            if (pendingJobs.decrementAndGet() == 0) {
                pendingJobs.notifyAll();
            }
        }
    }
}
