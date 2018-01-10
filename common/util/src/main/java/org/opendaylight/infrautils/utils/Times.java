/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import static java.util.Objects.requireNonNull;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for java.time.
 *
 * <p>The {@link #chronoUnit(TimeUnit)} and {@link #timeUnit(ChronoUnit)} methods are missing in Java 8,
 * but will be added in Java 9 (at which point this can deprecated and forward to those); see
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8141452">JDK-8141452</a> and
 * <a href="http://www.threeten.org/threeten-extra/">ThreeTen Extra</a>.
 */
public final class Times {

     /**
     * Converts a {@code TimeUnit} to a {@code ChronoUnit}.
     *
     * <p>This handles the seven units declared in {@code TimeUnit}.
     *
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     */
    public static ChronoUnit chronoUnit(TimeUnit unit) {
        requireNonNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalArgumentException("Unknown TimeUnit constant");
        }
    }

    /**
     * Converts a {@code ChronoUnit} to a {@code TimeUnit}.
     *
     * <p>This handles the seven units declared in {@code TimeUnit}.
     *
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     * @throws IllegalArgumentException if the unit cannot be converted
     */
    public static TimeUnit timeUnit(ChronoUnit unit) {
        requireNonNull(unit, "unit");
        switch (unit) {
            case NANOS:
                return TimeUnit.NANOSECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
            default:
                throw new IllegalArgumentException("ChronoUnit cannot be converted to TimeUnit: " + unit);
        }
    }

}
