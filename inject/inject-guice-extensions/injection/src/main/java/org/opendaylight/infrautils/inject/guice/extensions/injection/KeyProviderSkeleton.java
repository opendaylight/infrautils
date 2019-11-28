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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class KeyProviderSkeleton<A extends Annotation> implements KeyProvider<A> {

    @Override
    public Key<?> getKey(TypeLiteral<?> injectedType, Field injectedMember, A resourceAnnotation) {
        for (Annotation annotation : injectedMember.getAnnotations()) {
            if (Annotations.isBindingAnnotation(annotation.annotationType())) {
                return Key.get(injectedType.getFieldType(injectedMember), annotation);
            }
        }
        return Key.get(injectedType.getFieldType(injectedMember));
    }

    @Override
    public List<Key<?>> getParameterKeys(TypeLiteral<?> injectedType, Method injectedMember, A resourceAnnotation) {
        return Reflect.getParameterKeys(injectedType, injectedMember);
    }

}
