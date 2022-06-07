/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils.mockito;

import org.mockito.AdditionalAnswers;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * More {@link Mockito} {@link Answer} variants, extending the its standard
 * {@link Answers} and {@link AdditionalAnswers}.
 *
 * @author Michael Vorburger
 */
public final class MoreAnswers {
    private MoreAnswers() {
        // Hidden on purpose
    }

    /**
     * Returns Mockito Answer (default) which forwards method calls or throws an UnstubbedMethodException.
     *
     * @see CallsRealOrExceptionAnswer
     */
    @SuppressWarnings("unchecked")
    public static <T> Answer<T> realOrException() {
        return (Answer<T>) CallsRealOrExceptionAnswer.INSTANCE;
    }

    /**
     * Returns Mockito Answer (default) which throws an UnstubbedMethodException.
     *
     * @see ThrowsMethodExceptionAnswer
     */
    @SuppressWarnings("unchecked")
    public static <T> Answer<T> exception() {
        return (Answer<T>) ThrowsMethodExceptionAnswer.INSTANCE;
    }
}
