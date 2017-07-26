/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import java.util.Map;
import org.slf4j.MDC;

/**
 * Utility methods for {@link MDC}.
 *
 * @author Michael Vorburger.ch
 */
public final class MDCs {

    private MDCs() {
    }

    /**
     * Minor improvement over {@link MDC#put(String, String)}. Specifically, it:
     * <ul>
     * <li>tests if there already is an entry for the given <code>key</code> (to
     * detect forgotten <code>remove</code>, instead of overwritting)
     * <li>does not accept <code>null</code> as <code>val</code>
     * </ul>
     */
    public static void put(String key, String val) throws IllegalArgumentException {
        String oldValue = MDC.get(key);
        if (oldValue != null) {
            throw new IllegalArgumentException("MDC key " + key + " cannot be set to new value " + val
                    + " cauz it already has a value: " + oldValue);
        }
        if (val == null) {
            throw new IllegalArgumentException("MDC value cannot be null, for key: " + key);
        }
        MDC.put(key, val);
    }

    /**
     * {@link #put(String, String)} a key/val into the MDC, runs the provided
     * {@link Runnable}, and (ensure that) the given key is
     * {@link MDC#remove(String)}'d (even if there was an exception).
     */
    public static void putRunRemove(String key, String val, Runnable runnable) {
        MDCs.put(key, val);
        try {
            runnable.run();
        } finally {
            MDC.remove(key);
        }
    }

    /**
     * {@link #put(String, String)} each key/val of the passed Map into the MDC, runs the provided
     * {@link Runnable}, and (ensure that) the all keys of the Map are
     * {@link MDC#remove(String)}'d (even if there was an exception).
     */
    public static void putRunRemove(Map<String, String> keysValues, Runnable runnable) {
        keysValues.forEach((key, val) -> MDCs.put(key, val));
        try {
            runnable.run();
        } finally {
            keysValues.forEach((key, val) -> MDC.remove(key));
        }
    }

    // TODO putRunRemove which returns value from a passed Function (?)
}
