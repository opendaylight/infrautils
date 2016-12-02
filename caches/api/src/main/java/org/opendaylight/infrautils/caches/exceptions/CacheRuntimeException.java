/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.exceptions;

import org.opendaylight.infrautils.caches.Cache;

/**
 * {@link RuntimeException} wrapper thrown by {@link Cache#get(Object)} if either the
 * cache's function threw a RuntimeException, or the Cache implementation throws
 * an internal exception.
 *
 * @author Michael Vorburger.ch
 */
public class CacheRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 5678718966955298047L;

    public CacheRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
