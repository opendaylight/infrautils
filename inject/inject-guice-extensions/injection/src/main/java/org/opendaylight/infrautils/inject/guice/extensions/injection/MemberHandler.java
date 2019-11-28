/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.injection;

import com.google.inject.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface MemberHandler<A extends Annotation, M extends Member & AnnotatedElement> {
    void handle(TypeLiteral<?> type, Object instance, M member, A annotation);
}
