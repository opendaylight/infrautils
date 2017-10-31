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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages multiple ReentrantLocks by key.
 *
 * @param <T> the key type
 * @author Thomas Pantelis
 */
public class KeyedLock<T> {
    private static final Logger LOG = LoggerFactory.getLogger(KeyedLock.class);

    private static class CountingReentrantLock extends ReentrantLock {
        private static final long serialVersionUID = 1;

        AtomicInteger useCount = new AtomicInteger(1);
    }

    @GuardedBy("locks")
    private final Map<T, CountingReentrantLock> locks = new HashMap<>();

    public boolean tryLock(@Nonnull T lockKey, long time, TimeUnit unit) {
        LOG.debug("tryLock {}, time {}, unit: {}", lockKey, time, unit);
        return doLock(lockKey, lock -> {
            try {
                final boolean locked = lock.tryLock(time, unit);
                if (!locked) {
                    lock.useCount.decrementAndGet();
                    LOG.debug("tryLock {} - already locked - count: {}", lockKey, lock.useCount);
                }

                return locked;
            } catch (InterruptedException e) {
                lock.useCount.decrementAndGet();
                LOG.debug("tryLock {} interrrupted - count: {}", lockKey, lock.useCount);
                return false;
            }
        });
    }

    public boolean tryLock(@Nonnull T lockKey) {
        LOG.debug("tryLock {}", lockKey);
        return doLock(lockKey, lock -> {
            final boolean locked = lock.tryLock();
            if (!locked) {
                lock.useCount.decrementAndGet();
                LOG.debug("tryLock {} - already locked - count: {}", lockKey, lock.useCount);
            }

            return locked;
        });
    }

    public void lock(@Nonnull T lockKey) {
        LOG.debug("lock {}", lockKey);
        doLock(lockKey, lock -> {
            lock.lock();
            return true;
        });
    }

    private boolean doLock(T lockKey, Function<CountingReentrantLock, Boolean> lockFunction) {
        CountingReentrantLock lock;
        synchronized (locks) {
            lock = locks.get(lockKey);
            if (lock == null) {
                LOG.debug("Creating new Lock for {}", lockKey);
                CountingReentrantLock newLock = new CountingReentrantLock();
                locks.put(lockKey, newLock);
                return lockFunction.apply(newLock);
            } else {
                lock.useCount.incrementAndGet();
                LOG.debug("Lock for {} aleady exists - new count {}", lockKey, lock.useCount);
            }
        }

        return lockFunction.apply(lock);
    }

    public void unlock(@Nonnull T lockKey) {
        LOG.debug("unlock {}", lockKey);
        CountingReentrantLock lock = null;
        synchronized (locks) {
            lock = locks.get(lockKey);
            if (lock != null) {
                LOG.debug("Found Lock for {} - count: {}", lockKey, lock.useCount);
                if (lock.useCount.decrementAndGet() <= 0) {
                    LOG.debug("Rmoving Lock for {}", lockKey);
                    locks.remove(lockKey);
                }
            }
        }

        if (lock != null) {
            lock.unlock();
        } else {
            LOG.debug("Lock not found for {}", lockKey);
        }
    }

    @VisibleForTesting
    int size() {
        synchronized (locks) {
            return locks.size();
        }
    }
}
