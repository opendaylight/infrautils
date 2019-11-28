/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
