/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages multiple re-rentrant locks by name.
 *
 * @author Thomas Pantelis
 */
public class LockManager {
    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public boolean tryLock(String lockName, long time, TimeUnit unit) {
        try {
            return locks.computeIfAbsent(lockName, key -> new ReentrantLock()).tryLock(time, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean tryLock(String lockName) {
        return locks.computeIfAbsent(lockName, key -> new ReentrantLock()).tryLock();
    }

    public void lock(String lockName) {
        locks.computeIfAbsent(lockName, key -> new ReentrantLock()).lock();
    }

    public void unlock(String lockName) {
        final ReentrantLock lock = locks.get(lockName);
        if (lock != null) {
            lock.unlock();
        }
    }
}
