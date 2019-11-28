/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import com.google.inject.TypeLiteral;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.PreDestroy;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MethodHandler;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MethodInvoker;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Jsr250PreDestroyHandler implements MethodHandler<PreDestroy> {
    @Override
    public void handle(TypeLiteral<?> type, Object instance, Method method, PreDestroy annotation) {
        if (!Modifier.isStatic(method.getModifiers())) {
            MethodInvoker.on(method).invoke(instance);
        }
    }
}
