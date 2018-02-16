/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.tests;

import java.io.FileNotFoundException;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.infrautils.utils.Optionals;

/**
 * Unit test for {@link Optionals}.
 *
 * @author Michael Vorburger.ch
 */
public class OptionalsTests {

    // This just tests compilation, not actual behavior

    @Test
    public void testIfPresentWithoutException() {
        Optionals.ifPresent(Optional.<String>empty(), String::length);
    }

    @Test
    public void testIfPresentWithException() throws FileNotFoundException {
        Optionals.ifPresent(Optional.<String>empty(), s -> {
            throw new FileNotFoundException("");
        });
    }

}
