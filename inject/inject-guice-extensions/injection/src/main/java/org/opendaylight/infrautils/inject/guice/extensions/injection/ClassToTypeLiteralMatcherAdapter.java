/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import java.lang.reflect.AnnotatedElement;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com) date 2013-07-21
 */
public class ClassToTypeLiteralMatcherAdapter extends AbstractMatcher<TypeLiteral<?>> {
    private final Matcher<TypeLiteral<?>> matcher;

    ClassToTypeLiteralMatcherAdapter(final Matcher<AnnotatedElement> classMatcher) {
        this.matcher = new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            public boolean matches(TypeLiteral<?> typeL) {
                return classMatcher.matches(typeL.getRawType());
            }
        };
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        return matcher.matches(typeLiteral);
    }

    public static Matcher<TypeLiteral<?>> adapt(Matcher<AnnotatedElement> classMatcher) {
        return new ClassToTypeLiteralMatcherAdapter(classMatcher);
    }
}
