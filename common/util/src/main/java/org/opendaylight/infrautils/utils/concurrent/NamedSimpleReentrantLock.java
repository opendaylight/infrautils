/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ReentrantLock} which has a name and provides simplified locking operations.
 *
 * @param <T> the name type, required to be effectively immutable where T.hashCode() and T.equals() is concerned
 * @author Robert Varga
 */
public final class NamedSimpleReentrantLock<T> extends ReentrantLock {
    /**
     * Base result of a locking operation. It indicates whether the operation was successful via {@link #wasAcquired()}.
     * Once the lock (acquired or otherwise) is no longer need, {@link #close()} needs to be invoked. This is typically
     * done through a try-with-resources block.
     */
    public abstract static class AcquireResult implements AutoCloseable {
        /**
         * Indication whether the lock operation succeeded, in which case this method returns true. This does not mean
         * that the lock is still being held.
         *
         * @return True if the lock was successfully acquired by the operation which produced this object.
         */
        public abstract boolean wasAcquired();

        /**
         * Revert the lock operation. If {@link #wasAcquired()} returns true, this method releases the underlying lock
         * in an idempotent way. If {@link #wasAcquired()} returns false, this method does nothing.
         */
        @Override
        public abstract void close();
    }

    /**
     * Result of a successful lock operation, representing a single lease on the lock.
     */
    public static final class Acquired extends AcquireResult {
        @SuppressWarnings({ "null", "rawtypes" })
        private static final AtomicReferenceFieldUpdater<Acquired, @Nullable NamedSimpleReentrantLock> UPDATER =
                (AtomicReferenceFieldUpdater<Acquired, @Nullable NamedSimpleReentrantLock>)
                AtomicReferenceFieldUpdater.newUpdater(Acquired.class, NamedSimpleReentrantLock.class, "lock");

        @SuppressWarnings("unused")
        private volatile @Nullable NamedSimpleReentrantLock<?> lock;

        Acquired(NamedSimpleReentrantLock<?> lock) {
            this.lock = requireNonNull(lock);
        }

        @Override
        public boolean wasAcquired() {
            return true;
        }

        @Override
        public void close() {
            // This is quite a costly guard, alternative is:
            //    lock.unlock();
            //    lock = null;
            // i.e. throwing a NPE if weirdness happens
            ReentrantLock local = UPDATER.getAndSet(this, null);
            if (local != null) {
                local.unlock();
            } else {
                LOG.warn("Attempted to perform another unlock", new Throwable());
            }
        }
    }

    private static final class NotAcquired extends AcquireResult {
        @Override
        public boolean wasAcquired() {
            return false;
        }

        @Override
        public void close() {
            // Intentional no-op
        }
    }

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(NamedSimpleReentrantLock.class);
    private static final NotAcquired NOT_ACQUIRED = new NotAcquired();

    private final T name;

    public NamedSimpleReentrantLock(T name) {
        this.name = requireNonNull(name);
    }

    public T getName() {
        return name;
    }

    /**
     * Tries to acquire the lock for the given key if it is not held by another thread within the given waiting time.
     * See {@link ReentrantLock#tryLock(long, TimeUnit)} for more details.
     *
     * <p>
     * Example of use::
     * <pre>
     *     NamedSimpleReentrantLocks&lt;?&gt; lock;
     *
     *     try (AcquireResult lock = locks.tryAcquire(1, TimeUnit.SECONDS)) {
     *         // maybe locked region, not safe
     *
     *         if (lock.wasAcquired()) {
     *             // locked region
     *         } else {
     *             // unlocked region
     *         }
     *     });
     *
     *     // lock released
     * </pre>
     *
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return lock operation result. If the lock was free and was acquired by the current thread,
     *         {@link AcquireResult#wasAcquired()} will return true. If it reports false, the locking attempt has
     *         failed, either due to lock time expiring or the thread being interrupted while waiting.
     */
    public AcquireResult tryAcquire(long timeout, TimeUnit unit) {
        LOG.trace("tryLock {}, time {}, unit: {}", name, timeout, unit);

        boolean locked;
        try {
            locked = tryLock(timeout, unit);
        } catch (InterruptedException e) {
            LOG.warn("tryLock was interrrupted for key {}", name, e);
            return NOT_ACQUIRED;
        }

        return locked ? new Acquired(this) : NOT_ACQUIRED;
    }

    /**
     * Acquires the lock for the given key only if it is not held by another thread at the time of invocation.
     * See {@link #tryLock()} for more details.
     *
     * <p>
     * Example of use::
     * <pre>
     *     NamedSimpleReentrantLocks&lt;?&gt; lock;
     *
     *     try (AcquireResult lock = locks.tryAcquire()) {
     *         // maybe locked region
     *
     *         if (lock.wasAcquired()) {
     *             // locked region
     *         } else {
     *             // unlocked region
     *         }
     *     });
     *
     *     // lock released
     * </pre>
     *
     * @return lock operation result. If the lock was free and was acquired by the current thread,
     *         {@link AcquireResult#wasAcquired()} will return true. If it reports false, the locking attempt has
     *         failed,because another thread is currently holding the lock.
     */
    public AcquireResult tryAcquire() {
        LOG.trace("tryLock {}", name);
        return tryLock() ? new Acquired(this) : NOT_ACQUIRED;
    }

    /**
     * Acquires the lock. See {@link #lock()} for more details. Example of use:
     * <pre>
     *     NamedSimpleReentrantLocks&lt;?&gt; lock;
     *
     *     try (Acquired lock = locks.acquire()) {
     *          // locked region
     *     }
     *
     *     // lock released
     * </pre>
     */
    public Acquired acquire() {
        lock();
        return new Acquired(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).toString();
    }
}
