/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

import java.util.regex.Pattern;

/**
 * Validations for "ID" like simple types.
 *
 * @author Michael Vorburger.ch
 */
public final class IDs {
    private static final Pattern AZ09_LOWERCASE_REGEXP = Pattern.compile("[a-z][a-z0-9]*");
    private static final Pattern AZ09_REGEXP = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
    private static final Pattern AZ_09_REGEXP = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
    private static final Pattern AZ09DOT_REGEXP = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\.]*");

    private IDs() {

    }

    /**
     * Validate that ID matches regular expression <code>[a-z][a-z0-9]*</code>
     * (lower case letter or digits, but not start with digit, no dots).
     *
     * @throws IllegalArgumentException if not matching expected reg exp.
     */
    public static void checkOnlyLowercaseAZ09(String id) {
        check(id, AZ09_LOWERCASE_REGEXP);
    }

    /**
     * Validate that ID matches regular expression <code>[a-zA-Z][a-zA-Z0-9]*</code>
     * (upper or lower case letter or digits, but not start with digit, no dots).
     *
     * @throws IllegalArgumentException if not matching expected reg exp.
     */
    public static void checkOnlyAZ09(String id) {
        check(id, AZ09_REGEXP);
    }

    /**
     * Validate that ID matches regular expression
     * <code>[a-zA-Z][a-zA-Z0-9_]*</code> (underscores allowed, but not starting with; no dots).
     *
     * @throws IllegalArgumentException if not matching expected reg exp.
     */
    public static void checkOnlyAZ09Underscore(String id) {
        check(id, AZ_09_REGEXP);
    }

    /**
     * Validate that ID matches regular expression
     * <code>[a-zA-Z0-9][a-zA-Z0-9.]*</code> (dots allowed, but not starting with).
     *
     * @throws IllegalArgumentException if not matching expected reg exp.
     */
    public static void checkOnlyAZ09Dot(String id) {
        check(id, AZ09DOT_REGEXP);
    }

    /**
     * Validate that ID matches regexp Pattern passed as argument.
     * @throws IllegalArgumentException if not matching expected reg exp.
     */
    public static void check(String id, Pattern regexp) {
        if (!regexp.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Invalid ID: \"" + id + "\"" + " must match regular expression " + regexp.pattern());
        }
    }
}
