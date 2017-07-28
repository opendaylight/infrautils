package org.opendaylight.infrautils.testutils.concurrent;

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
public class AwaitableExecutorService implements ExecutorService {
    private final ExecutorService delegate;
    private final AtomicLong pendingJobs = new AtomicLong(0);

    public AwaitableExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        synchronized (pendingJobs) {
            pendingJobs.incrementAndGet();
            return delegate.submit(new AwaitableCallable<>(task));
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        synchronized (pendingJobs) {
            pendingJobs.incrementAndGet();
            return delegate.submit(new AwaitableRunnable(task), result);
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        synchronized (pendingJobs) {
            pendingJobs.incrementAndGet();
            return delegate.submit(new AwaitableRunnable(task));
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        pendingJobs.addAndGet(tasks.size());
        // invokeAll can’t be run with a lock that’s also involved in the tasks
        return delegate.invokeAll(tasks.stream().map(AwaitableCallable::new).collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        pendingJobs.addAndGet(tasks.size());
        // invokeAll can’t be run with a lock that’s also involved in the tasks
        return delegate.invokeAll(tasks.stream().map(AwaitableCallable::new).collect(Collectors.toList()), timeout,
                unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        pendingJobs.addAndGet(tasks.size());
        // invokeAny can’t be run with a lock that’s also involved in the tasks
        return delegate.invokeAny(tasks.stream().map(AwaitableCallable::new).collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        pendingJobs.addAndGet(tasks.size());
        // invokeAny can’t be run with a lock that’s also involved in the tasks
        return delegate.invokeAny(tasks.stream().map(AwaitableCallable::new).collect(Collectors.toList()), timeout,
                unit);
    }

    @Override
    public void execute(Runnable command) {
        synchronized (pendingJobs) {
            pendingJobs.incrementAndGet();
            delegate.execute(new AwaitableRunnable(command));
        }
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
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        synchronized (pendingJobs) {
            while (System.currentTimeMillis() < end && !isCompleted()) {
                pendingJobs.wait(end - System.currentTimeMillis());
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

    private class AwaitableCallable<T> implements Callable<T> {
        private final Callable<T> task;

        private AwaitableCallable(Callable<T> task) {
            this.task = task;
        }

        @Override
        public T call() throws Exception {
            try {
                return task.call();
            } finally {
                synchronized (pendingJobs) {
                    pendingJobs.decrementAndGet();
                    pendingJobs.notifyAll();
                }
            }
        }
    }

    private class AwaitableRunnable implements Runnable {
        private final Runnable task;

        private AwaitableRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.run();
            } finally {
                synchronized (pendingJobs) {
                    pendingJobs.decrementAndGet();
                    pendingJobs.notifyAll();
                }
            }
        }
    }
}
