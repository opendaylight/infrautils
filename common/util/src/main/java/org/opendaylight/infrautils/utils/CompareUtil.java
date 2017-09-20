/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

public final class CompareUtil {

    private CompareUtil() {
    }

    public static boolean safeCompare(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static int safeCompareTo(Comparable obj1, Comparable obj2) {
        if (obj1 == obj2) {
            return 0;
        }
        if (obj1 == null) {
            return 1;
        }
        return obj2 == null ? -1 : obj1.compareTo(obj2);
    }
}
