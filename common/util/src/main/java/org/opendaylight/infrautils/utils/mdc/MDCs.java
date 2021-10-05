/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.mdc;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

/**
 * Utility methods for {@link MDC}.
 *
 * @author Michael Vorburger.ch
 * @deprecated This class is not used anywhere and therefore cannot mature. It will be removed in a future release.
 */
@Beta
@Deprecated(since = "2.0.7", forRemoval = true)
// Runnable as last argument is clearer to read, but interferes with vararg
// in putRunRemove(Runnable runnable, MDCEntry... keysValues)
@SuppressWarnings("InconsistentOverloads")
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
     *
     * @throws IllegalArgumentException
     *             if val is value is already set (and was previously not
     *             {@link MDC#remove(String)})
     */
    public static void put(String key, String val) {
        validate(key, val);
        MDC.put(key, val);
    }

    private static void validate(String key, String val) {
        requireNonNull(key, "key");
        requireNonNull(val, () -> "MDC value cannot be null, for key: " + key);
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

    public static void putRunRemove(MDCEntry entry, Runnable runnable) {
        putRunRemove(entry.mdcKeyString(), entry.mdcValueString(), runnable);
    }

    /**
     * {@link #put(String, String)} each key/val of the passed Map into the MDC, runs the provided
     * {@link Runnable}, and (ensure that) the all keys of the Map are
     * {@link MDC#remove(String)}'d (even if there was an exception).
     */
    public static void putRunRemove(Map<String, String> keysValues, Runnable runnable) {
        keysValues.forEach(MDCs::put);
        try {
            runnable.run();
        } finally {
            keysValues.keySet().forEach(MDC::remove);
        }
    }

    public static void putRunRemove(Runnable runnable, MDCEntry... keysValues) {
        for (MDCEntry mdcEntry : keysValues) {
            MDCs.put(mdcEntry.mdcKeyString(), mdcEntry.mdcValueString());
        }
        try {
            runnable.run();
        } finally {
            for (MDCEntry mdcEntry : keysValues) {
                MDC.remove(mdcEntry.mdcKeyString());
            }
        }
    }

    // TODO putRunRemove which returns value from a passed Function (?)
}
