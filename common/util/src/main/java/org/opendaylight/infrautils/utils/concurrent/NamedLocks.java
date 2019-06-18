/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;
import com.google.errorprone.annotations.Var;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.opendaylight.infrautils.utils.concurrent.NamedSimpleReentrantLock.AcquireResult;
import org.opendaylight.infrautils.utils.concurrent.NamedSimpleReentrantLock.Acquired;

/**
 * Manages multiple ReentrantLocks identified by a unique name.
 *
 * <p>
 * Example of use of lock():
 * <pre>
 *     NamedLocks&lt;String&gt; locks;
 *     String lockName;
 *
 *     try (Acquired lock = locks.Acquire(lockName)) {
 *          // locked region
 *     }
 *
 *     // lock released
 * </pre>
 *
 * <p>
 * Example of use of tryLock():
 * <pre>
 *     NamedLocks&lt;String&gt; locks;
 *     String lockName;
 *
 *     try (AcquireResult lock = locks.tryAcquire(lockName)) {
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
 * @param <T> the name type, required to be effectively immutable where T.hashCode() and T.equals() is concerned
 * @author Robert Varga
 */
public final class NamedLocks<T> {
    private static final class WeakRef<T>
            extends FinalizableWeakReference<NamedSimpleReentrantLock<T>> {
        private final ConcurrentHashMap<T, WeakRef<T>> locks;
        private final T name;

        WeakRef(NamedSimpleReentrantLock<T> lock, FinalizableReferenceQueue queue,
                ConcurrentHashMap<T, WeakRef<T>> locks) {
            super(lock, queue);
            this.locks = requireNonNull(locks);
            this.name = lock.getName();
        }

        @Override
        public void finalizeReferent() {
            locks.remove(name, this);
        }
    }

    private final FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
    private final ConcurrentHashMap<T, WeakRef<T>> locks = new ConcurrentHashMap<>();

    /**
     * Tries to acquire the lock for the given key if it is not held by another thread within the given waiting time.
     * See {@link ReentrantLock#tryLock(long, TimeUnit)} for more details.
     *
     * @param lockName the key to lock
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return lock operation result. If the lock was free and was acquired by the current thread,
     *         {@link AcquireResult#wasAcquired()} will return true. If it reports false, the locking attempt failed,
     *         either due to lock time expiring or the thread being interrupted while waiting.
     */
    public AcquireResult tryAcquire(T lockName, long timeout, TimeUnit unit) {
        return getLock(lockName).tryAcquire(timeout, unit);
    }

    /**
     * Acquires the lock for the given key only if it is not held by another thread at the time of invocation.
     * See {@link ReentrantLock#tryLock()} for more details.
     *
     * @param lockName the key to lock
     * @return lock operation result. If the lock was free and was acquired by the current thread,
     *         {@link AcquireResult#wasAcquired()} will return true. If it reports false, the locking attempt failed,
     *         because another thread is currently holding the lock.
     */
    public AcquireResult tryAcquire(T lockName) {
        return getLock(lockName).tryAcquire();
    }

    /**
     * Acquires the lock for the given key.
     * See {@link ReentrantLock#lock()} for more details.
     *
     * @param lockKey the key to lock
     */
    public Acquired acquire(T lockKey) {
        return getLock(lockKey).acquire();
    }

    public NamedSimpleReentrantLock<T> getLock(T lockKey) {
        while (true) {
            // computeIfAbsent() locks the computation bucket, hence we are doing things with get/putIfAbsent. This
            // also has the benefit of removing an edge case where our new lock would immediately be GC'd.
            @Var WeakRef<T> ref = locks.get(lockKey);
            if (ref == null) {
                // No entry found, attempt to insert a new one. Note we retain the lock reference on stack, so it cannot
                // be GC'd while it is being inserted
                NamedSimpleReentrantLock<T> newLock = new NamedSimpleReentrantLock<>(lockKey);
                ref = locks.putIfAbsent(lockKey, new WeakRef<>(newLock, queue, locks));
                if (ref == null) {
                    // Successful insert, return newly-allocated lock
                    return newLock;
                }
            }

            // Entry found, but it may have expired
            NamedSimpleReentrantLock<T> lock = ref.get();
            if (lock != null) {
                return lock;
            }

            // The lock has been cleared by GC, but the reference has not been processed yet. Finalize the reference,
            // which removes it from locks.
            ref.finalizeReferent();
        }
    }
}
