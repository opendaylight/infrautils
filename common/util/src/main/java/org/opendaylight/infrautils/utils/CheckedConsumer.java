/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.util.function.Consumer;

/**
 * {@link Consumer} like {@link FunctionalInterface} which can throw checked exceptions.
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

    // TODO remove when https://git.eclipse.org/r/#/c/105982/ is available

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(T value) throws E;

}
