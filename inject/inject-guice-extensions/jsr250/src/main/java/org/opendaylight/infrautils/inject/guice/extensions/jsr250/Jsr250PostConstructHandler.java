/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MethodHandler;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MethodInvoker;
import org.opendaylight.infrautils.inject.guice.extensions.injection.Reflect;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Jsr250PostConstructHandler implements MethodHandler<PostConstruct> {

    @Inject
    Provider<Injector> injector;

    @Override
    public void handle(TypeLiteral<?> type, Object instance, Method method, PostConstruct annotation) {
        if (!Modifier.isStatic(method.getModifiers())) {
            List<Key<?>> parameterKeys = Reflect.getParameterKeys(type, method);
            Object[] parameters = new Object[parameterKeys.size()];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = injector.get().getProvider(parameterKeys.get(i)).get();
            }
            MethodInvoker.on(method).invoke(instance, parameters);
        }
    }

}
