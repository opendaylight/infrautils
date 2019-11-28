/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class FieldHandlerTypeListener<A extends Annotation> implements TypeListener {
    private final Class<A> annotationType;
    private final Class<? extends FieldHandler<A>> handlerClass;

    public FieldHandlerTypeListener(Class<A> annotationType, Class<? extends FieldHandler<A>> handlerClass) {
        this.annotationType = annotationType;
        this.handlerClass = handlerClass;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> type, TypeEncounter<I> encounter) {
        final Provider<? extends FieldHandler<A>> provider = encounter.getProvider(handlerClass);
        final List<Field> fields = Lists
                .newLinkedList(Reflect.findAllAnnotatedFields(type.getRawType(), annotationType));
        if (!fields.isEmpty()) {
            encounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(I injectee) {
                    FieldHandler<A> handler = provider.get();
                    for (Field field : fields) {
                        handler.handle(type, injectee, field, field.getAnnotation(annotationType));
                    }
                }
            });
        }
    }
}
