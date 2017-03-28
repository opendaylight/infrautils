/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TablePrinter {
    private static final Logger LOG = LoggerFactory.getLogger(TablePrinter.class);

    private static int SPACE_BETWEEN_COLUMNS = 1;
    private static int SPACE_BEFORE_TABLES_WITH_TITLE = 4;

    @RegEx
    private static final String DPLUS_STR = "^\\d+$";
    private static final Pattern DPLUS = Pattern.compile(DPLUS_STR);

    @RegEx
    private static final String  DPLUS_DPLUS_STR = "^\\D+\\d+$";
    private static final Pattern DPLUS_DPLUS = Pattern.compile(DPLUS_DPLUS_STR);

    private int ncols;
    private final List<String[]> table = new ArrayList<>();
    private String title = null;
    private String[] header = null;
    private Comparator<String[]> comparator;

    public TablePrinter(final int sortByColumn) {
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
                        if (compareInt == 0) {
                            // identical numbers, move to next column
                            continue;
                        } else {
                            return compareInt;
                        }
                    }

                    if (DPLUS_DPLUS.matcher(o1[i]).matches() && DPLUS_DPLUS.matcher(o2[i]).matches()) {
                        // strings are strings with trailing numbers (e.g. "odl2 and odl10")
                        String o1StringPart = o1[i].replaceAll("\\d+$", ""); // remove digits from end of string
                        String o2StringPart = o2[i].replaceAll("\\d+$", ""); // remove digits from end of string
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
                String numStr = str.replaceAll("^\\D*", ""); // remove non-digits
                // return 0 if no digits found
                try {
                    int num = Integer.parseInt(numStr);
                    return num;
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

    public void setColumnNumber(int ncols) {
        this.ncols = ncols;
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

        if (comparator != null) {
            sort();
        }

        printTitle(sb);
        printHeader(separator, maxWidths, sb);
        for (String[] row : table) {
            if (title != null) {
                sb.append(StringUtils.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            printRow(separator, maxWidths, sb, row);
        }

        return sb.toString();
    }

    public void sort() {
        Collections.sort(table, comparator);
    }


    private void printTitle(StringBuilder sb) {
        if (title != null) {
            sb.append(title).append(":").append("\n");
        }
    }

    private void printHeader(String separator, int[] maxWidths, StringBuilder sb) {
        if (header != null) {
            if (title != null) {
                sb.append(StringUtils.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            printRow(separator, maxWidths, sb, header);
            if (title != null) {
                sb.append(StringUtils.repeat(" ", SPACE_BEFORE_TABLES_WITH_TITLE));
            }
            printHeaderUnderline(separator, maxWidths, sb);
        }
    }

    private void printHeaderUnderline(String separator, int[] maxWidths, StringBuilder sb) {
        int rowLength = SPACE_BETWEEN_COLUMNS + separator.length() * (header.length - 1) + sum(maxWidths);
        sb.append(StringUtils.repeat("-", rowLength));
        sb.append("\n");
    }

    private static int sum(final int[] array) {
        int ret = 0;
        for (int n : array) {
            ret += n;
        }
        return ret;
    }

    private static void printRow(final String separator, final int[] maxWidths, final StringBuilder sb,
            final String[] row) {
        for (int i = 0; i < row.length; i++) {
            printSeparator(separator, sb, i);
            sb.append(row[i]);
            sb.append(StringUtils.repeat(" ", maxWidths[i] - row[i].length()));
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

    private static void considerRow(final int[] maxWidths, final String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (row[i].length() > maxWidths[i]) {
                maxWidths[i] = row[i].length();
            }
        }
    }

    private static void printSeparator(final String separator, final StringBuilder sb, final int integer) {
        if (integer == 0) {
            sb.append(StringUtils.repeat(" ", SPACE_BETWEEN_COLUMNS));
        } else {
            sb.append(separator);
        }
    }

    public String columnSeparator() {
        String space = StringUtils.repeat(" ", SPACE_BETWEEN_COLUMNS);
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
