/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TablePrinter {
    private static final Logger LOG = LoggerFactory.getLogger(TablePrinter.class);

    private static final int SPACE_BETWEEN_COLUMNS = 1;
    private static final int SPACE_BEFORE_TABLES_WITH_TITLE = 4;

    @Regex
    private static final String DPLUS_STR = "^\\d+$";
    private static final Pattern DPLUS = Pattern.compile(DPLUS_STR);

    @Regex
    private static final String  DPLUS_DPLUS_STR = "^\\D+\\d+$";
    private static final Pattern DPLUS_DPLUS = Pattern.compile(DPLUS_DPLUS_STR);

    @Regex
    private static final String DPLUS_REMOVE_STR = "\\d+$";
    private static final Pattern DPLUS_REMOVE = Pattern.compile(DPLUS_REMOVE_STR);

    @Regex
    private static final String NON_DIGITS_STR = "^\\D*";
    private static final Pattern NON_DIGITS = Pattern.compile(NON_DIGITS_STR);

    private final List<String[]> table = new ArrayList<>();
    private final Comparator<String[]> comparator;

    private String title = null;
    private String[] header = null;
    private int ncols;

    public TablePrinter(int sortByColumn) {
        this.comparator = new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                for (int i = sortByColumn; i < o1.length && i < o2.length; i++) {
                    int compareStr = o1[i].compareTo(o2[i]);
                    if (compareStr == 0) {
                        // identical strings, move to next column
                        continue;
                    }

                    if (DPLUS.matcher(o1[i]).matches() && DPLUS.matcher(o2[i]).matches()) {
                        // strings are actually numbers, compare numbers
                        int compareInt = extractInt(o1[i]) - extractInt(o2[i]);
                        if (compareInt != 0) {
                            return compareInt;
                        }

                        // identical numbers, move to next column
                        continue;
                    }

                    if (DPLUS_DPLUS.matcher(o1[i]).matches() && DPLUS_DPLUS.matcher(o2[i]).matches()) {
                        // strings are strings with trailing numbers (e.g. "odl2 and odl10"), remove them from the end
                        String o1StringPart = DPLUS_REMOVE.matcher(o1[i]).replaceAll("");
                        String o2StringPart = DPLUS_REMOVE.matcher(o2[i]).replaceAll("");
                        if (o1StringPart.equals(o2StringPart)) {
                            // string parts are identical, compare integers
                            int compareInt = extractInt(o1[i]) - extractInt(o2[i]);
                            if (compareInt != 0) {
                                return compareInt;
                            }
                        }
                    }
                    return compareStr;
                }
                return 0;
            }

            int extractInt(String str) {
                String numStr = NON_DIGITS.matcher(str).replaceAll(""); // remove non-digits
                // return 0 if no digits found
                try {
                    return Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    LOG.warn("Received unexpected NumberFormatException when trying to parse string {} into an integer",
                            numStr);
                    return 0;
                }
            }
        };
    }

    public TablePrinter() {
        this(0);
    }

    public void setColumnNumber(int columnNumber) {
        this.ncols = columnNumber;
    }

    public void addRow(Object... array) {
        String[] newLine = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && !array[i].toString().isEmpty()) {
                newLine[i] = array[i].toString();
            } else {
                newLine[i] = "N/A";
            }
        }
        table.add(newLine);
    }

    @Override
    public String toString() {
        String separator = columnSeparator();
        int[] maxWidths = calculateWidths();
        StringBuilder sb = new StringBuilder();

        table.sort(comparator);

        printTitle(sb);
        printHeader(separator, maxWidths, sb);
        for (String[] row : table) {
            if (title != null) {
                sb.append(Strings.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            printRow(separator, maxWidths, sb, row);
        }

        return sb.toString();
    }

    private void printTitle(StringBuilder sb) {
        if (title != null) {
            sb.append(title).append(":").append("\n");
        }
    }

    private void printHeader(String separator, int[] maxWidths, StringBuilder sb) {
        if (header != null) {
            if (title != null) {
                sb.append(Strings.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            printRow(separator, maxWidths, sb, header);
            if (title != null) {
                sb.append(Strings.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            // Header underline
            int rowLength = SPACE_BETWEEN_COLUMNS + separator.length() * (header.length - 1) + sum(maxWidths);
            sb.append(Strings.repeat("-", rowLength));
            sb.append("\n");
        }
    }

    private static int sum(int[] array) {
        @Var int ret = 0;
        for (int n : array) {
            ret += n;
        }
        return ret;
    }

    private static void printRow(String separator, int[] maxWidths, StringBuilder sb, String[] row) {
        for (int i = 0; i < row.length; i++) {
            printSeparator(separator, sb, i);
            sb.append(row[i]);
            sb.append(Strings.repeat(" ", maxWidths[i] - row[i].length()));
        }
        sb.append("\n");
    }

    private int[] calculateWidths() {
        int[] maxWidths = new int[ncols];

        for (String[] row : table) {
            considerRow(maxWidths, row);
        }

        if (header != null) {
            considerRow(maxWidths, header);
        }
        return maxWidths;
    }

    private static void considerRow(int[] maxWidths, String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i].length() > maxWidths[i]) {
                maxWidths[i] = row[i].length();
            }
        }
    }

    private static void printSeparator(String separator, StringBuilder sb, int integer) {
        if (integer == 0) {
            sb.append(Strings.repeat(" ", SPACE_BETWEEN_COLUMNS));
        } else {
            sb.append(separator);
        }
    }

    private static String columnSeparator() {
        String space = Strings.repeat(" ", SPACE_BETWEEN_COLUMNS);
        return space + "|" + space;
    }

    public void setColumnNames(String... names) {
        header = names;
        ncols = names.length;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
