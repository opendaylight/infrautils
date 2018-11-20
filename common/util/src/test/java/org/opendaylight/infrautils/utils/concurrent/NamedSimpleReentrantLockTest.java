/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.errorprone.annotations.Var;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.utils.concurrent.NamedSimpleReentrantLock.AcquireResult;
import org.opendaylight.infrautils.utils.concurrent.NamedSimpleReentrantLock.Acquired;

public class NamedSimpleReentrantLockTest {
    private static class LockerThread extends Thread {
        final NamedSimpleReentrantLock<?> testLock = new NamedSimpleReentrantLock<>("foo");

        private final Object lock = new Object();
        private volatile boolean shutdown;

        @Override
        public void run() {
            testLock.lock();

            synchronized (lock) {
                try {
                    while (!shutdown) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }

            testLock.unlock();
        }

        void unlock() {
            synchronized (lock) {
                shutdown = true;
                lock.notify();
            }
        }
    }

    @Before
    public void before() {

    }

    @Test
    public void testBoilerplate() {
        NamedSimpleReentrantLock<String> lock = new NamedSimpleReentrantLock<>("foo");
        assertEquals("foo", lock.getName());
        assertEquals("NamedSimpleReentrantLock{name=foo}", lock.toString());
    }

    @Test
    public void testAcquire() throws InterruptedException {
        NamedSimpleReentrantLock<String> lock = new NamedSimpleReentrantLock<>("foo");
        Acquired acq = lock.acquire();
        assertTrue(acq.wasAcquired());
        assertTrue(lock.isHeldByCurrentThread());

        acq.close();
        assertFalse(lock.isHeldByCurrentThread());
        assertFalse(lock.isLocked());

        acq.close();
        assertFalse(lock.isHeldByCurrentThread());
        assertFalse(lock.isLocked());
    }

    @Test
    public void testTryAcquire() throws InterruptedException {
        LockerThread thread = new LockerThread();
        @Var
        AcquireResult acq = thread.testLock.tryAcquire();
        assertTrue(acq.wasAcquired());
        assertTrue(thread.testLock.isHeldByCurrentThread());

        acq.close();
        assertFalse(thread.testLock.isHeldByCurrentThread());
        assertFalse(thread.testLock.isLocked());

        thread.setDaemon(true);
        thread.start();

        try {
            while (!thread.testLock.isLocked()) {
                Thread.sleep(1);
            }

            acq = thread.testLock.tryAcquire();
            assertFalse(acq.wasAcquired());
            assertFalse(thread.testLock.isHeldByCurrentThread());
            acq.close();
        } finally {
            thread.unlock();
            thread.join();
        }
    }

    @Test
    public void testTryAcquireTime() throws InterruptedException {
        LockerThread thread = new LockerThread();
        @Var
        AcquireResult acq = thread.testLock.tryAcquire(5, TimeUnit.MILLISECONDS);
        assertTrue(acq.wasAcquired());
        assertTrue(thread.testLock.isHeldByCurrentThread());

        acq.close();
        assertFalse(thread.testLock.isHeldByCurrentThread());
        assertFalse(thread.testLock.isLocked());

        thread.setDaemon(true);
        thread.start();

        try {
            while (!thread.testLock.isLocked()) {
                Thread.sleep(1);
            }

            acq = thread.testLock.tryAcquire(5, TimeUnit.MILLISECONDS);
            assertFalse(acq.wasAcquired());
            assertFalse(thread.testLock.isHeldByCurrentThread());
            acq.close();
        } finally {
            thread.unlock();
            thread.join();
        }
    }
}
