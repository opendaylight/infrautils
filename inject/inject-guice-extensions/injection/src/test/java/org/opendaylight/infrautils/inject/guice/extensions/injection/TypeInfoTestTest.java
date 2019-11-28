/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com) date 2013-07-21
 */
@RunWith(JUnit4.class)
public class TypeInfoTestTest {

    @Test
    public void test() throws Exception {
        //assertEquals(15, describe(Reflect.findAllMethods(getClass())).size());
        //assertEquals(0, describe(Reflect.findAllFields(getClass())).size());
        assertEquals(1, describe(Reflect.findAllAnnotatedMethods(getClass(), Test.class)).size());
        //assertEquals(1, describe(Reflect.findAllAnnotatedInvokables(getClass(), Test.class)).size());
    }

    private <T> Collection<T> describe(Collection<T> clsses) {
        return clsses;
    }

    private <T> Collection<T> describe(Iterable<T> clss) {
        return describe(Lists.newLinkedList(clss));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public TypeInfoTestTest() {
        super();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}