/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches;

import java.util.Optional;

/**
 * Exception thrown when a {@link CacheFunction} returns a null value,
 * which it never should. Consider using {@link Optional} as Value of the Cache
 * if its Function may not have values for some keys.  This is a RuntimeException
 * because it always indicates a programmer's error in code (a bug).  User code
 * invoking {@link Cache#get(Object)} should typically NOT catch this Exception,
 * but let it propagate.
 *
 * @author Michael Vorburger.ch
 */
public class BadCacheFunctionRuntimeException extends RuntimeException {
    // TODO Thoughts, anyone, how about making this extends Error instead of RT, and rename?

    private static final long serialVersionUID = -7240903317644417618L;

    public BadCacheFunctionRuntimeException(String message) {
        super(message);
    }

    // This is used in corner cases for other situations than null value
    // for example, see CacheGuavaAdapter's get(Iterable<? extends K> keys) handling of ExecutionException
    public BadCacheFunctionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
