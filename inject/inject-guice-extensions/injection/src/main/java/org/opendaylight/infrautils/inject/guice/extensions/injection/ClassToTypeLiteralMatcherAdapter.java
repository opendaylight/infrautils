/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *<p/>
 *This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
