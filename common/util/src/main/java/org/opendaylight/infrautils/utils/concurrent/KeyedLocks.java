/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages multiple ReentrantLocks by key.
 *
 * @param <T> the key type
 * @author Thomas Pantelis
 * @deprecated Use {@link NamedLocks} instead.
 */
@Deprecated
public class KeyedLocks<T> {
    @FunctionalInterface
    private interface BooleanLockFunction<T> {
        boolean apply(T key, CountingReentrantLock lock);
    }

    private static class CountingReentrantLock extends ReentrantLock {
        private static final long serialVersionUID = 1;

        final AtomicInteger useCount = new AtomicInteger(1);
    }

    private static final Logger LOG = LoggerFactory.getLogger(KeyedLocks.class);

    @GuardedBy("locks")
    private final Map<T, CountingReentrantLock> locks = new HashMap<>();

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
    public boolean tryLock(@Nonnull T lockKey, long timeout, TimeUnit unit) {
        LOG.debug("tryLock {}, time {}, unit: {}", lockKey, timeout, unit);
        return doLock(lockKey, (key, lock) -> {
            try {
                boolean locked = lock.tryLock(timeout, unit);
                if (!locked) {
                    lock.useCount.decrementAndGet();
                    LOG.debug("tryLock {} - already locked - count: {}", key, lock.useCount);
                }

                return locked;
            } catch (InterruptedException e) {
                lock.useCount.decrementAndGet();
                LOG.warn("tryLock was interrrupted for key {} - remaining use count: {}", key, lock.useCount);
                return false;
            }
        });
    }

    /**
     * Acquires the lock for the given key only if it is not held by another thread at the time of invocation.
     * See {@link ReentrantLock#tryLock()} for more details.
     *
     * @param lockKey the key to lock
     * @return {@code true} if the lock was free and was acquired by the current thread, or the lock was already held
     *         by the current thread, otherwise {@code false} otherwise
     */
    public boolean tryLock(@Nonnull T lockKey) {
        LOG.debug("tryLock {}", lockKey);
        return doLock(lockKey, (key, lock) -> {
            boolean locked = lock.tryLock();
            if (!locked) {
                lock.useCount.decrementAndGet();
                LOG.debug("tryLock {} - already locked - count: {}", key, lock.useCount);
            }

            return locked;
        });
    }

    /**
     * Acquires the lock for the given key.
     * See {@link ReentrantLock#lock()} for more details.
     *
     * @param lockKey the key to lock
     */
    public void lock(@Nonnull T lockKey) {
        LOG.debug("lock {}", lockKey);
        doLock(lockKey, (key, lock) -> {
            lock.lock();
            return true;
        });
    }

    private boolean doLock(T lockKey, BooleanLockFunction<T> lockFunction) {
        CountingReentrantLock lock;
        synchronized (locks) {
            lock = locks.get(lockKey);
            if (lock == null) {
                LOG.debug("Creating new Lock for {}", lockKey);
                CountingReentrantLock newLock = new CountingReentrantLock();
                locks.put(lockKey, newLock);
                return lockFunction.apply(lockKey, newLock);
            } else {
                lock.useCount.incrementAndGet();
                LOG.debug("Lock for {} aleady exists - new count {}", lockKey, lock.useCount);
            }
        }

        return lockFunction.apply(lockKey, lock);
    }

    /**
     * Attempts to release the lock for the given key.
     * See {@link ReentrantLock#unlock()} for more details.
     *
     * @param lockKey the key to unlock
     * @throws IllegalMonitorStateException if the current thread does not hold this lock
     */
    public void unlock(@Nonnull T lockKey) {
        LOG.debug("unlock {}", lockKey);
        CountingReentrantLock lock;
        synchronized (locks) {
            lock = locks.get(lockKey);
            if (lock != null) {
                LOG.debug("Found Lock for {} - count: {}", lockKey, lock.useCount);
                if (lock.useCount.decrementAndGet() <= 0) {
                    LOG.debug("Rmoving Lock for {}", lockKey);
                    locks.remove(lockKey);
                }
            } else {
                throw new IllegalMonitorStateException("Lock for key " + lockKey
                        + " is not owned by the current thread");
            }
        }

        lock.unlock();
    }

    @VisibleForTesting
    int size() {
        synchronized (locks) {
            return locks.size();
        }
    }
}
