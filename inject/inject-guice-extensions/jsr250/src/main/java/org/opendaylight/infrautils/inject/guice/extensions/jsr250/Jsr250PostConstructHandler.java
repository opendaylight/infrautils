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
