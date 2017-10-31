/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * Unit tests for KeyedLocks.
 *
 * @author Thomas Pantelis
 */
public class KeyedLocksTest {
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private final KeyedLocks<String> keyedLocks = new KeyedLocks<>();

    @Test(timeout = 20000)
    public void testLock() throws InterruptedException {
        keyedLocks.lock(KEY1);
        assertEquals("KeyedLock size", 1, keyedLocks.size());

        keyedLocks.lock(KEY2);
        assertEquals("KeyedLock size", 2, keyedLocks.size());

        keyedLocks.unlock(KEY1);
        keyedLocks.unlock(KEY2);
        assertEquals("KeyedLock size", 0, keyedLocks.size());

        final AtomicReference<Throwable> uncaughtException = new AtomicReference<>();
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch thread1Locked = new CountDownLatch(1);
        final CountDownLatch thread1Continue = new CountDownLatch(1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                keyedLocks.lock(KEY1);
                thread1Locked.countDown();

                counter.incrementAndGet();
                Uninterruptibles.awaitUninterruptibly(thread1Continue);

                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

                keyedLocks.unlock(KEY1);
            }
        };

        thread1.setUncaughtExceptionHandler((th, ex) -> uncaughtException.set(ex));
        thread1.start();

        assertTrue("Lock on thread 1 did not complete", thread1Locked.await(3, TimeUnit.SECONDS));

        final CountDownLatch thread2AtLock = new CountDownLatch(1);
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                thread2AtLock.countDown();
                keyedLocks.lock(KEY1);

                counter.incrementAndGet();

                keyedLocks.unlock(KEY1);
            }
        };

        thread2.setUncaughtExceptionHandler((th, ex) -> uncaughtException.set(ex));
        thread2.start();

        assertTrue("Thread 2 did not reach lock", thread1Locked.await(3, TimeUnit.SECONDS));
        thread1Continue.countDown();

        thread1.join(3000);
        assertEquals("Thread isAlive", false, thread1.isAlive());

        thread2.join(3000);
        assertEquals("Thread isAlive", false, thread2.isAlive());

        if (uncaughtException.get() != null) {
            throw new AssertionError("Thread threw exception", uncaughtException.get());
        }

        assertEquals("Counter", 2, counter.get());

        assertEquals("KeyedLock size", 0, keyedLocks.size());
    }

    @Test(timeout = 20000)
    public void testTryLock() throws InterruptedException {
        boolean locked = keyedLocks.tryLock(KEY1);
        assertTrue("Expected tryLock to return true", locked);
        assertEquals("KeyedLock size", 1, keyedLocks.size());

        keyedLocks.unlock(KEY1);
        assertEquals("KeyedLock size", 0, keyedLocks.size());

        locked = keyedLocks.tryLock(KEY1, 200, TimeUnit.MILLISECONDS);
        assertTrue("Expected tryLock to return true", locked);
        assertEquals("KeyedLock size", 1, keyedLocks.size());

        final AtomicReference<Throwable> uncaughtException = new AtomicReference<>();
        final CountDownLatch thread1AtTryLock = new CountDownLatch(1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                thread1AtTryLock.countDown();

                boolean threadLocked = keyedLocks.tryLock(KEY1);
                assertFalse("Expected tryLock to return false", threadLocked);
                assertEquals("KeyedLock size", 1, keyedLocks.size());

                threadLocked = keyedLocks.tryLock(KEY1, 200, TimeUnit.MILLISECONDS);
                assertFalse("Expected tryLock to return false", threadLocked);
                assertEquals("KeyedLock size", 1, keyedLocks.size());
            }
        };

        thread1.setUncaughtExceptionHandler((th, ex) -> uncaughtException.set(ex));
        thread1.start();

        assertTrue("Thread 2 did not reach tryLock", thread1AtTryLock.await(3, TimeUnit.SECONDS));

        thread1.join(3000);
        assertEquals("Thread isAlive", false, thread1.isAlive());

        if (uncaughtException.get() != null) {
            throw new AssertionError("Thread threw exception", uncaughtException.get());
        }

        keyedLocks.unlock(KEY1);

        assertEquals("KeyedLock size", 0, keyedLocks.size());
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testUnlockWhenNotLocked() {
        keyedLocks.unlock(KEY1);
    }
}
