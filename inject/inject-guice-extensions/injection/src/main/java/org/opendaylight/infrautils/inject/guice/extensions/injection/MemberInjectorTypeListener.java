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

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class MemberInjectorTypeListener<A extends Annotation> implements TypeListener {

    private final Class<A> annotationType;
    private final Class<? extends KeyProvider<A>> providerClass;

    public MemberInjectorTypeListener(Class<A> annotationType, Class<? extends KeyProvider<A>> providerClass) {
        this.annotationType = annotationType;
        this.providerClass = providerClass;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @Override
    public <I> void hear(final TypeLiteral<I> injectableType, TypeEncounter<I> encounter) {
        final Provider<? extends KeyProvider<A>> provider = encounter.getProvider(providerClass);
        final Provider<Injector> injectorProvider = encounter.getProvider(Injector.class);
        final List<Field> fields = Lists
                .newLinkedList(Reflect.findAllAnnotatedFields(injectableType.getRawType(), annotationType));
        final List<MethodInvoker> methods = Lists
                .newLinkedList(Reflect.findAllAnnotatedInvokables(injectableType.getRawType(), annotationType));
        if (!fields.isEmpty() || !methods.isEmpty()) {
            encounter.register(new MembersInjector<I>() {
                @SuppressWarnings("unchecked")
                @Override
                public void injectMembers(I injectee) {
                    KeyProvider<A> keyProvider = provider.get();
                    // inject fields
                    for (Field field : fields) {
                        Object value = injectorProvider.get()
                                .getProvider(
                                        keyProvider.getKey(injectableType, field, field.getAnnotation(annotationType)))
                                .get();
                        if (!field.isAccessible()) {
//                            field.setAccessible(true);
                            AccessController.doPrivileged((PrivilegedAction) () -> {
                                field.setAccessible(true);
                                return null;
                            });
                        }
                        try {
                            field.set(injectee, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(
                                    "Failed to inject field " + field + ". Reason: " + e.getMessage(), e);
                        }
                    }
                    // inject methods
                    for (MethodInvoker invokable : methods) {
                        List<Key<?>> parameterKeys = keyProvider.getParameterKeys(injectableType, invokable.method,
                                invokable.getAnnotation(annotationType));
                        Object[] parameters = new Object[parameterKeys.size()];
                        for (int i = 0; i < parameters.length; i++) {
                            parameters[i] = injectorProvider.get().getProvider(parameterKeys.get(i)).get();
                        }
                        try {
                            invokable.invoke(injectee, parameters);
                        } catch (Exception ex) {
                            throw MycilaGuiceException.toRuntime(ex);
                        }
                    }
                }
            });
        }
    }

}
