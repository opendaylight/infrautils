/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Objects;

public class CompareUtil {

    /**
     * Null safe equals.
     * NB: This method is misnamed, it's a safe equals, not compare.
     *
     * @deprecated Prefer using the {@link Objects#equals(Object, Object)} (or
     *             {@link Objects#deepEquals(Object, Object) for Arrays})
     */
    @Deprecated
    public static boolean safeCompare(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

    /**
     * Null safe compareTo.
     * Consider using JDK's
     * {@link Objects#compare(Object, Object, java.util.Comparator)} or Guava's
     * {@link ComparisonChain} and {@link Ordering} instead of this.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static int safeCompareTo(Comparable obj1, Comparable obj2) {
        if (obj1 == obj2) {
            return 0;
        }
        if (obj1 == null && obj2 != null) {
            return 1;
        } else if (obj1 != null && obj2 == null) {
            return -1;
        }
        return obj1.compareTo(obj2);
    }

    public static <T> void calculateDiff(Collection<T> newCollection, Collection<T> oldCollection, Collection<T> added,
            Collection<T> removed, Collection<T> retain) {
        retain.addAll(newCollection);
        added.addAll(newCollection);
        removed.addAll(oldCollection);
        retain.retainAll(oldCollection);
        for (T oldElement : oldCollection) {
            added.remove(oldElement);
        }

        for (T newElement : newCollection) {
            removed.remove(newElement);
        }
    }

}
