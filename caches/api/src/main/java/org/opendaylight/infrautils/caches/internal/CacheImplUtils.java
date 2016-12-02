/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods, shamlessly stolen from Guava.
 *
 * @author Michael Vorburger.ch
 */
public final class CacheImplUtils {
    private CacheImplUtils() { }

    public static <K, V> Map<K, V> newLinkedHashMapWithExpectedSize(Iterable<? extends K> keys) {
        return newLinkedHashMapWithExpectedSize(size(keys));
    }

    // com.google.common.collect.Maps inspired, but changed HashMap to LinkedHashMap

    private static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
        return new LinkedHashMap<>(capacity(expectedSize));
    }

    // The following are copy/pasted here just to avoid a dependency to Guava
    // from the api module only for these two micro helper methods.

    // com.google.common.collect.Iterables

    private static int size(Iterable<?> iterable) {
        return iterable instanceof Collection
            ? ((Collection<?>) iterable).size()
            : size(iterable.iterator());
    }

    // com.google.common.collect.Iterators

    private static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    // com.google.common.collect.Maps

    private static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            // checkNonnegative(expectedSize, "expectedSize");
            return expectedSize + 1;
        }
        // if (expectedSize < Ints.MAX_POWER_OF_TWO) {
        return expectedSize + expectedSize / 3;
        // }
        // return Integer.MAX_VALUE; // any large value
    }

}
