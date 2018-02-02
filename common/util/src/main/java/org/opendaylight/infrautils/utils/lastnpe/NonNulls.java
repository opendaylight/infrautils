/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.lastnpe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility methods related to null analysis.
 *
 * @author Michael Vorburger.ch
 */
public final class NonNulls {

    private NonNulls() {
    }

    /**
     * Casts something declared Nullable to be NonNull, if code is 100% sure.
     *
     * <p>See
     * <a href="https://github.com/uber/NullAway/wiki/Suppressing-Warnings">NullAway
     * Suppressing Warnings documentation</a> and {@link org.eclipse.jdt.annotation.Checks#requireNonNull(Object)}.
     */
    public static @Nonnull <T> T castToNonNull(@Nullable T expectedNonNull) {
        if (expectedNonNull == null) {
            throw new IllegalArgumentException("Unexpected null");
        }
        return expectedNonNull;
    }

}
