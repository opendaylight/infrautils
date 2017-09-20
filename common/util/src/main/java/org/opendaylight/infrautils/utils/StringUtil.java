/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import javax.annotation.concurrent.GuardedBy;

public final class StringUtil {

    private StringUtil() {
    }

    @GuardedBy("DEFAULT_DATE_FORMAT")
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");

    public static final String ARRAY_SPLIT_CHAR = ";";
    public static final String GUI_ARRAY_SEPARATOR = ";";
    public static final char GUI_ARRAY_SEPARATOR_CHAR = GUI_ARRAY_SEPARATOR.charAt(0);

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String toString(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        string.append('[');
        int last = bytes.length - 1;
        for (int i = 0; i <= last; i++) {
            if (i > 0) {
                string.append(',');
            }
            if (i < last && bytes[i] == bytes[i + 1]) {
                int count = 1;
                while (i < last && bytes[i] == bytes[i + 1]) {
                    ++i;
                    ++count;
                }
                string.append(toString(bytes[i])).append('x').append(count);
            } else {
                string.append(toString(bytes[i]));
            }
        }
        string.append("] (byte[").append(bytes.length).append("])");
        return string.toString();
    }

    public static String toString(byte someByte) {
        int byteAsInt = someByte;
        byteAsInt += 0x100;
        String hex = Integer.toHexString(byteAsInt);
        return hex.substring(hex.length() - 2);
    }

    /**
     * A shared String comparator, equivalent to String::compareTo.
     *
     * @deprecated Use String::compareTo directly.
     */
    @Deprecated
    public static final Comparator<String> STRING_COMPARATOR_INSTANCE = String::compareTo;

    public static boolean isMatching(final String filter, final String[] filters) {
        if (filters == null) {
            return false;
        }
        for (String filterName : filters) {
            if (filter.matches(filterName)) {
                return true;
            }
        }
        return false;
    }
}
