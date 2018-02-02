/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.lastnpe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods related to null analysis.
 *
 * @author Michael Vorburger.ch
 */
public final class NonNulls {

    private NonNulls() {
    }

    /**
     * TODO Doc.
     *
     * <p>See
     * <a href="https://github.com/uber/NullAway/wiki/Suppressing-Warnings">NullAway
     * Suppressing Warnings documentation</a>.
     */
    public static <T> T castToNonNull(@Nullable T expectedNonNull) {
        if (expectedNonNull == null) {
            throw new IllegalArgumentException("Unexpected null");
        }
        return expectedNonNull;
    }

}
