/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import org.eclipse.jdt.annotation.Checks;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility to deal with {@link Nullable} references.
 * This is an extension of the {@link Checks} utility.
 * Methods here are proposed upstream there, but in the meantime live here.
 *
 * @author Michael Vorburger.ch
 */
public final class Nullables extends org.eclipse.jdt.annotation.Checks {

    // TODO not sure if I really want to extend Checks - or just copy/paste some but not all of it here?
    // E.g. requireNonNull() overlaps with java.util.Objects#requireNonNull() ... :-(

    private Nullables() {
    }

    // TODO remove when https://git.eclipse.org/r/#/c/105982/ is available
    public static <T, E extends Exception> void applyIfNonNull(@Nullable T value,
            CheckedConsumer<@NonNull ? super T, E> consumer) throws E {

        if (value != null) {
            consumer.accept(value);
        }
    }

}
