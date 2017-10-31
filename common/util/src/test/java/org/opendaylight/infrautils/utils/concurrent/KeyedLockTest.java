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
 * Unit tests for KeyedLock.
 *
 * @author Thomas Pantelis
 */
public class KeyedLockTest {
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private final KeyedLock<String> manager = new KeyedLock<>();

    @Test(timeout = 20000)
    public void testLock() throws InterruptedException {
        manager.lock(KEY1);
        assertEquals("KeyedLock size", 1, manager.size());

        manager.lock(KEY2);
        assertEquals("KeyedLock size", 2, manager.size());

        manager.unlock(KEY1);
        manager.unlock(KEY2);
        assertEquals("KeyedLock size", 0, manager.size());

        final AtomicReference<Throwable> uncaughtException = new AtomicReference<>();
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch thread1Locked = new CountDownLatch(1);
        final CountDownLatch thread1Continue = new CountDownLatch(1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                manager.lock(KEY1);
                thread1Locked.countDown();

                counter.incrementAndGet();
                Uninterruptibles.awaitUninterruptibly(thread1Continue);

                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

                manager.unlock(KEY1);
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
                manager.lock(KEY1);

                counter.incrementAndGet();

                manager.unlock(KEY1);
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

        // This should be a no-op although unbalanced locks/unlocks shouldn't occur.
        manager.unlock(KEY1);

        assertEquals("KeyedLock size", 0, manager.size());
    }

    @Test(timeout = 20000)
    public void testTryLock() throws InterruptedException {
        boolean locked = manager.tryLock(KEY1);
        assertTrue("Expected tryLock to return true", locked);
        assertEquals("KeyedLock size", 1, manager.size());

        manager.unlock(KEY1);
        assertEquals("KeyedLock size", 0, manager.size());

        locked = manager.tryLock(KEY1, 200, TimeUnit.MILLISECONDS);
        assertTrue("Expected tryLock to return true", locked);
        assertEquals("KeyedLock size", 1, manager.size());

        final AtomicReference<Throwable> uncaughtException = new AtomicReference<>();
        final CountDownLatch thread1AtTryLock = new CountDownLatch(1);
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                thread1AtTryLock.countDown();

                boolean threadLocked = manager.tryLock(KEY1);
                assertFalse("Expected tryLock to return false", threadLocked);
                assertEquals("KeyedLock size", 1, manager.size());

                threadLocked = manager.tryLock(KEY1, 200, TimeUnit.MILLISECONDS);
                assertFalse("Expected tryLock to return false", threadLocked);
                assertEquals("KeyedLock size", 1, manager.size());
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

        manager.unlock(KEY1);

        assertEquals("KeyedLock size", 0, manager.size());
    }
}
