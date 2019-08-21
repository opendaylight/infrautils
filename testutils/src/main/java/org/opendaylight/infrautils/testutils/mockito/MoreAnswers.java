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
import org.opendaylight.infrautils.testutils.Partials;

/**
 * More {@link Mockito} {@link Answer} variants, extending the its standard
 * {@link Answers} and {@link AdditionalAnswers}. Consider using the
 * {@link Partials#newPartial(Class)} short cut directly.
 *
 * @author Michael Vorburger
 */
@SuppressWarnings("unchecked")
public final class MoreAnswers {
    private MoreAnswers() {

    }

    /**
     * Returns Mockito Answer (default) which forwards method calls or throws an UnstubbedMethodException.
     *
     * @see CallsRealOrExceptionAnswer
     */
    public static <T> Answer<T> realOrException() {
        return (Answer<T>) CallsRealOrExceptionAnswer.INSTANCE;
    }

    /**
     * Returns Mockito Answer (default) which throws an UnstubbedMethodException.
     *
     * @see ThrowsMethodExceptionAnswer
     */
    public static <T> Answer<T> exception() {
        return (Answer<T>) ThrowsMethodExceptionAnswer.INSTANCE;
    }
}
