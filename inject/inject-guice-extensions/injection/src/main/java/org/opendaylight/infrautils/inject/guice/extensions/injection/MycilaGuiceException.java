/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class MycilaGuiceException extends RuntimeException {

    public MycilaGuiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MycilaGuiceException(Throwable cause) {
        super(cause.getMessage(), cause);
        setStackTrace(cause.getStackTrace());
    }

    public static RuntimeException toRuntime(Throwable throwable) {
        while (throwable instanceof InvocationTargetException || throwable instanceof ExecutionException
                || throwable instanceof MycilaGuiceException) {
            throwable = throwable instanceof InvocationTargetException
                    ? ((InvocationTargetException) throwable).getTargetException()
                    : throwable.getCause();
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new MycilaGuiceException(throwable);
    }
}
