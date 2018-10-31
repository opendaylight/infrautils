/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.util.concurrent.Callable;
import org.opendaylight.infrautils.utils.function.CheckedCallable;
import org.opendaylight.infrautils.utils.function.CheckedRunnable;

/**
 * Utilities related to {@link ClassLoader}.
 *
 * @author Michael Vorburger.ch
 */
public final class ClassLoaders {

    private ClassLoaders() { }

    public static void run(Runnable runnable, ClassLoader classLoader) {
        call((CheckedCallable<Void, RuntimeException>) () -> {
            runnable.run();
            return null;
        }, classLoader);
    }

    public static <E extends Exception> void run(CheckedRunnable<E> runnable, ClassLoader classLoader) throws E {
        call((CheckedCallable<Void, E>) () -> {
            runnable.run();
            return null;
        }, classLoader);
    }

    public static <V> V call(Callable<V> callable, ClassLoader classLoader) throws Exception {
        return call((CheckedCallable<V, Exception>) () -> callable.call(), classLoader);
    }

    public static <V, E extends Exception> V call(CheckedCallable<V, E> callable, ClassLoader classLoader) throws E {
        Thread thread = Thread.currentThread();
        ClassLoader originalContextClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            return callable.call();
        } finally {
            thread.setContextClassLoader(originalContextClassLoader);
        }
    }

    public static Runnable wrap(Runnable runnable, ClassLoader classLoader) {
        return () -> run(runnable, classLoader);
    }
}
