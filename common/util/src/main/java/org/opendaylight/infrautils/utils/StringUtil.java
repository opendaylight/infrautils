/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StringUtil {

    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final Integer[] EMPTY_INTEGER_ARRAY = {};
    private static final int[] EMPTY_INT_ARRAY = {};
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS z";

    public static final String ARRAY_SPLIT_CHAR = ";";
    public static final String GUI_ARRAY_SEPARATOR = ";";
    public static final char GUI_ARRAY_SEPARATOR_CHAR = GUI_ARRAY_SEPARATOR.charAt(0);

    public static final int NO_VALUE = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    private static final int MAX_ARGS_CHARS = 450;

    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return format.parse(dateStr);
    }

    public static boolean isStringRangeValid(String val, int min, int max) {
        int length = val == null ? 0 : val.length();

        if (length < min || length > max) {
            return false;
        }
        return true;
    }

    public static String methodAsString(String methodName, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    public static String asString(Date date) {
        DateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        String dateStr = format.format(date);
        return dateStr;
    }

    public static String asString(List<String> strList) {
        return asString(strList, ARRAY_SPLIT_CHAR);
    }

    public static String asString(List<String> strList, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String str : strList) {
            sb.append(str).append(delimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String asString(int[] intarr) {
        StringBuilder sb = new StringBuilder();
        for (int i : intarr) {
            sb.append(i).append(ARRAY_SPLIT_CHAR);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String asString(Object other) {
        return other == null ? "" : other.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static int asNumber(String str) {
        return Integer.valueOf(str.trim());
    }

    public static Integer asNullableNumber(String str) {
        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long asNullableLongNumber(String str) {
        try {
            return Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String asStr(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    public static String asNonEmptyStr(String str) {
        if (str == null) {
            return null;
        }
        String res = str.trim();
        if (res.length() == 0) {
            return null;
        }

        return res;
    }

    public static String asNonEmptyStrNoTrim(String str) {
        if (str == null) {
            return null;
        }

        if (str.trim().length() == 0) {
            return null;
        }

        return str;
    }

    public static String asEmptyStr(String str) {
        String res = asNonEmptyStr(str);
        return res == null ? "" : res;
    }

    public static String[] asArray(String inputArray) {
        if (inputArray == null || inputArray.trim().isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        return inputArray.split(ARRAY_SPLIT_CHAR);
    }

    public static Integer[] asIds(String inputArray) {
        if (isEmpty(inputArray)) {
            return EMPTY_INTEGER_ARRAY;
        }
        String[] strings = inputArray.split(ARRAY_SPLIT_CHAR);
        Integer[] integers = new Integer[strings.length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = asNumber(strings[i]);
        }
        return integers;
    }

    public static int[] asIntArray(String inputArray) {
        if (isEmpty(inputArray)) {
            return EMPTY_INT_ARRAY;
        }
        String[] strings = inputArray.split(ARRAY_SPLIT_CHAR);
        int[] integers = new int[strings.length];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = asNumber(strings[i]);
        }
        return integers;
    }

    public static String[] asStrArray(String inputArray) {
        if (isEmpty(inputArray)) {
            return EMPTY_STRING_ARRAY;
        }
        inputArray = inputArray.trim();
        if (!(inputArray.startsWith("[") && inputArray.endsWith("]"))) {
            throw new RuntimeException("String array incorrectly formatted");
        }
        return inputArray.substring(1, inputArray.length() - 1).split("(\\s*,\\s*)");
    }

    public static String unquote(String string) {
        if ((string.startsWith("\"") || string.startsWith("'")) && (string.endsWith("\"") || string.endsWith("'"))) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0))).append(str.substring(1))
                .toString();
    }

    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == NO_VALUE) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != NO_VALUE) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static Integer convertNoValue(int num) {
        return hasValue(num) ? num : null;
    }

    public static boolean hasValue(int num) {
        return num != NO_VALUE;
    }

    public static boolean emptyString(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        str = str.trim();
        return str.length() == 0;
    }

    public static String fullMethodSignatureAsString(Object target, Method method, Object[] args) {
        StringBuilder string = new StringBuilder();
        string.append(target.getClass().getSimpleName()).append("::").append(method.getName()).append('(');
        int preLength = string.length();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; args != null && i < args.length; i++) {
            if (i > 0) {
                string.append(", ");
            }
            if (args[i] == null) {
                string.append("null");
            } else if (types[i].equals(String.class)) {
                string.append('\"').append(String.valueOf(args[i]).replaceAll("\"", "\\\"")).append('\"');
            } else if (types[i].equals(Long.class) || types[i].equals(long.class)) {
                string.append(args[i]).append('L');
            } else if (String.class.equals(args[i].getClass().getComponentType())) {
                string.append("S").append(Arrays.toString((String[]) args[i]));
            } else if (int.class.equals(args[i].getClass().getComponentType())) {
                string.append("i").append(Arrays.toString((int[]) args[i]));
            } else if (boolean.class.equals(args[i].getClass().getComponentType())) {
                string.append("b").append(Arrays.toString((boolean[]) args[i]));
            } else if (long.class.equals(args[i].getClass().getComponentType())) {
                string.append("l").append(Arrays.toString((long[]) args[i]));
            } else if (args[i].getClass().isArray() && !args[i].getClass().getComponentType().isPrimitive()) {
                string.append("O").append(Arrays.toString((Object[]) args[i]));
            } else {
                string.append(args[i]);
            }
            if (string.length() - preLength > MAX_ARGS_CHARS) {
                break;
            }
        }
        if (string.length() - preLength > MAX_ARGS_CHARS) {
            int length = Math.min(string.length() - preLength, MAX_ARGS_CHARS);
            return string.subSequence(0, length) + "...)";
        }
        string.append(')');
        return string.toString();
    }

    public static String collect(Object... objects) {
        StringBuilder string = new StringBuilder();
        for (Object o : objects) {
            string.append(o);
        }
        return string.toString();
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
        string.append("] (byte[" + bytes.length + "])");
        return string.toString();
    }

    public static String toString(byte someByte) {
        int byteAsInt = someByte;
        byteAsInt += 0x100;
        String hex = Integer.toHexString(byteAsInt);
        return hex.substring(hex.length() - 2);
    }

    public static StringComparator STRING_COMPARATOR_INSTANCE = new StringComparator();

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    public static boolean isMatching(String filter, String[] filters) {
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

    public static int getNumOfPatternGroups(String patternStr) {
        int open = 0;
        int close = 0;
        if (patternStr != null) {
            for (int i = 0; i < patternStr.length(); i++) {
                char character = patternStr.charAt(i);
                if (character == '(') {
                    open++;
                } else if (character == ')') {
                    close++;
                }
            }
        }

        return open == close ? open : -1;
    }

    public static String convertAsciiStringToString(String inputInAscii) {
        if (inputInAscii == null || inputInAscii.isEmpty()) {
            return "";
        }

        String[] asciiValues = inputInAscii.split(",");
        byte[] value = new byte[asciiValues.length];

        for (int i = 0; i < asciiValues.length; i++) {
            value[i] = Byte.valueOf(asciiValues[i]);
        }

        return new String(value);
    }
}
