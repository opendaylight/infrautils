/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

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
     * detect forgotten <code>remove</code>, instead of overwriting)
     * <li>does not accept <code>null</code> as <code>val</code>
     * </ul>
     */
    public static void put(String key, String val) throws IllegalArgumentException {
        validate(key, val);
        MDC.put(key, val);
    }

    private static void validate(String key, String val) throws IllegalArgumentException {
        Preconditions.checkArgument(val != null, "MDC value cannot be null, for key: %s", key);
        String oldValue = MDC.get(key);
        // If the IllegalArgumentException is ever proving to be too much of a nuisance, we should at the very least
        // keep it as a WARN log, but never completely remove it... If this occurs, it really is
        // an indication of a bug with someone having forgotten to remove an MDC key where it should have been,
        // and that really should be fixed.
        Preconditions.checkArgument(oldValue == null || oldValue.equals(val),
                "MDC key %s cannot be set to new value %s cauz it already has another value: %s", key, val, oldValue);
    }

    /**
     * {@link #put(String, String)} a key/val into the MDC, runs the provided
     * {@link Runnable}, and (ensure that) the given key is
     * {@link MDC#remove(String)}'d (even if there was an exception).
     */
    public static void putRunRemove(String key, String val, Runnable runnable) {
        validate(key, val);
        try (MDCCloseable closeable = MDC.putCloseable(key, val)) {
            runnable.run();
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
            keysValues.keySet().forEach(MDC::remove);
        }
    }

    // TODO putRunRemove which returns value from a passed Function (?)
}
