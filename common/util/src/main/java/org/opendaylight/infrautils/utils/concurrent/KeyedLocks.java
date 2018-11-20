/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages multiple ReentrantLocks by key.
 *
 * <p>
 * Example of use of lock():
 * <pre>
 *     KeyedLocks<String> locks;
 *     String lockName;
 *
 *     try (AcquiredLock lock = locks.lock(lockName)) {
 *          // locked region
 *     }
 *
 *     // lock released
 * </pre>
 *
 * <p>
 * Example of use of tryLock():
 * <pre>
 *     KeyedLocks<String> locks;
 *     String lockName;
 *
 *     try (LockResult lock = locks.tryLock(lockName)) {
 *         // maybe locked region
 *
 *         if (lock.wasAcquired()) {
 *             // locked region
 *         }
 *     });
 *
 *     // lock released
 * </pre>
 *
 * @param <T> the key type
 * @author Thomas Pantelis
 */
public class KeyedLocks<T> {
    public abstract static class LockResult implements AutoCloseable {
        public abstract boolean wasAcquired();

        @Override
        public abstract void close();
    }

    public static final class Acquired extends LockResult {
        @SuppressWarnings("null")
        private static final AtomicReferenceFieldUpdater<Acquired, @Nullable ReentrantLock> UPDATER =
                (AtomicReferenceFieldUpdater<Acquired, @Nullable ReentrantLock>)
                AtomicReferenceFieldUpdater.newUpdater(Acquired.class, ReentrantLock.class, "lock");

        @SuppressWarnings("unused")
        private volatile @Nullable ReentrantLock lock;

        Acquired(final ReentrantLock lock) {
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
            final ReentrantLock local = UPDATER.getAndSet(this, null);
            if (local != null) {
                local.unlock();
            } else {
                LOG.warn("Attempted to perform another unlock", new Throwable());
            }
        }
    }

    private static final class NotAcquired extends LockResult {
        @Override
        public boolean wasAcquired() {
            return false;
        }

        @Override
        public void close() {
            // Intentional no-op
        }
    }

    private final class WeakRef extends FinalizableWeakReference<ReentrantLock> {
        private final T key;

        WeakRef(final T key) {
            super(new ReentrantLock(), queue);
            this.key = requireNonNull(key);
        }

        @Override
        public void finalizeReferent() {
            locks.remove(key, this);
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(KeyedLocks.class);
    private static final NotAcquired NOT_ACQUIRED = new NotAcquired();

    final FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
    final Map<T, WeakRef> locks = new ConcurrentHashMap<>();

    /**
     * Tries to acquire the lock for the given key if it is not held by another thread within the given waiting time.
     * See {@link ReentrantLock#tryLock(long, TimeUnit)} for more details.
     *
     * @param lockKey the key to lock
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return {@code true} if the lock was free and was acquired by the current thread, or the lock was already held
     *         by the current thread, otherwise {@code false} if the waiting time elapsed before the lock could be
     *         acquired or the current thread was interrupted.
     */
    public LockResult tryLock(@Nonnull final T lockKey, final long timeout, final TimeUnit unit) {
        LOG.debug("tryLock {}, time {}, unit: {}", lockKey, timeout, unit);

        final ReentrantLock lock = getLock(lockKey);
        final boolean locked;

        try {
            locked = lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            LOG.warn("tryLock was interrrupted for key {}", lockKey, e);
            return NOT_ACQUIRED;
        }

        return locked ? new Acquired(lock) : NOT_ACQUIRED;
    }

    /**
     * Acquires the lock for the given key only if it is not held by another thread at the time of invocation.
     * See {@link ReentrantLock#tryLock()} for more details.
     *
     * @param lockKey the key to lock
     * @return {@code true} if the lock was free and was acquired by the current thread, or the lock was already held
     *         by the current thread, otherwise {@code false} otherwise
     */
    public LockResult tryLock(@Nonnull final T lockKey) {
        LOG.debug("tryLock {}", lockKey);
        final ReentrantLock lock = getLock(lockKey);
        return lock.tryLock() ? new Acquired(lock) : NOT_ACQUIRED;
    }

    /**
     * Acquires the lock for the given key.
     * See {@link ReentrantLock#lock()} for more details.
     *
     * @param lockKey the key to lock
     */
    public Acquired lock(@Nonnull final T lockKey) {
        LOG.debug("lock {}", lockKey);
        final ReentrantLock lock = getLock(lockKey);
        lock.lock();
        return new Acquired(lock);
    }

    // This might be useful for advanced users, who use this just as a T -> ReentrantLock cache
    private ReentrantLock getLock(final T lockKey) {
        while (true) {
            final WeakRef ref = locks.computeIfAbsent(lockKey, key -> new WeakRef(key));
            final ReentrantLock lock = ref.get();
            if (lock != null) {
                return lock;
            }

            // The lock is gone, retry
            locks.remove(lockKey, ref);
        }
    }

    @VisibleForTesting
    long size() {
        return locks.size();
    }
}
