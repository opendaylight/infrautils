/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

import java.util.concurrent.Callable;

/**
 * Runnable which can throw any checked Exception, but does not return a value.
 *
 * @see Runnable
 * @see Callable
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
@SuppressWarnings("checkstyle:IllegalThrows")
public interface CheckedRunnable<T extends Throwable> {

    void run() throws T;

}
