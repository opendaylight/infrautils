/**
 *<p/>Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com)
 *<p/>
 *<p/>Licensed under the Apache License, Version 2.0 (the "License");
 *<p/>you may not use this file except in compliance with the License.
 *<p/>You may obtain a copy of the License at
 *<p/>
 *<p/>     http://www.apache.org/licenses/LICENSE-2.0
 *<p/>
 *<p/>Unless required by applicable law or agreed to in writing, software
 *<p/>distributed under the License is distributed on an "AS IS" BASIS,
 *<p/>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *<p/>See the License for the specific language governing permissions and
 *<p/>limitations under the License.
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