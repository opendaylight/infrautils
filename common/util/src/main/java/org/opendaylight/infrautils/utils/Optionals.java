/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.util.Optional;
import org.opendaylight.infrautils.utils.function.CheckedConsumer;

/**
 * Utilities for Optionals.
 *
 * @author Michael Vorburger.ch
 * @deprecated This class is not used anywhere and does not bring much to the table anyway.
 */
@Deprecated(forRemoval = true)
public final class Optionals {

    private Optionals() {
    }

    /**
     * Like {@link Optional#ifPresent(java.util.function.Consumer)} but allows checked exceptions to be thrown.
     */
    public static <T, E extends Exception> void ifPresent(Optional<T> optional, CheckedConsumer<? super T, E> consumer)
            throws E {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
    }
}
