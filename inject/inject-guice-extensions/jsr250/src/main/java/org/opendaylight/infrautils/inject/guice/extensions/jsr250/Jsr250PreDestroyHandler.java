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
