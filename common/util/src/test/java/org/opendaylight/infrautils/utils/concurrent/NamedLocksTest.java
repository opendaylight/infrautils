/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.concurrent;

import static org.junit.Assert.assertSame;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.utils.concurrent.NamedSimpleReentrantLock.Acquired;

@Deprecated(since = "2.0.7", forRemoval = true)
public class NamedLocksTest {
    private @Nullable NamedLocks<String> locks;

    @Before
    public void before() {
        locks = new NamedLocks<>();
    }

    @Test
    public void testLookup() {
        NamedSimpleReentrantLock<String> first = locks.getLock("foo");
        NamedSimpleReentrantLock<String> second = locks.getLock("foo");
        assertSame(first, second);
    }

    @Test
    public void testMultiAcquire() {
        Acquired first = locks.acquire("foo");
        Acquired second = locks.acquire("foo");
        first.close();
        second.close();
    }
}
