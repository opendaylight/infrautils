/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.nonnull.tests;

import static com.google.common.collect.Lists.newArrayList;
import static org.opendaylight.infrautils.utils.nonnull.NullSafeCollections.wrapAsNullSafeList;

import org.junit.Test;
import org.opendaylight.infrautils.utils.nonnull.NullSafeCollections;

/**
 * Unit Test for {@link NullSafeCollections}.
 *
 * @author Michael Vorburger.ch
 */
public class NullSafeCollectionsTest {

    @Test
    public void testDelegateEmpty() {
        wrapAsNullSafeList(newArrayList());
    }

    @Test
    public void testDelegateOK() {
        wrapAsNullSafeList(newArrayList("hello", "world"));
    }

    @Test(expected = NullPointerException.class)
    public void testDelegateAlreadyHasSomeNull() {
        wrapAsNullSafeList(newArrayList("hello", null, "world"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullDelegate() {
        wrapAsNullSafeList(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAddNull() {
        wrapAsNullSafeList(newArrayList("hello", "world")).add(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAddNullWithIndex() {
        wrapAsNullSafeList(newArrayList("hello", "world")).add(0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAddCollectionContainingNull() {
        wrapAsNullSafeList(newArrayList("hello", "world")).addAll(newArrayList("hello", null, "world"));
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAddCollectionContainingNullWithIndex() {
        wrapAsNullSafeList(newArrayList("hello", "world")).addAll(0, newArrayList("hello", null, "world"));
    }

}
