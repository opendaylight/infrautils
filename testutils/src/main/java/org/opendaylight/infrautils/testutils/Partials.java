/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.testutils;

import org.mockito.Mockito;
import org.opendaylight.infrautils.testutils.mockito.MoreAnswers;

/**
 * Creates instances of "partial" (abstract) test doubles.
 *
 * @author Michael Vorburger.ch
 */
public final class Partials {
    private Partials() {

    }

    public static <T> T newPartial(Class<T> abstractClass) {
        return Mockito.mock(abstractClass, MoreAnswers.realOrException());
    }
}
