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

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.ProviderInstanceBinding;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.InjectorCloseListener;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MBinder;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MethodHandler;
import org.opendaylight.infrautils.inject.guice.extensions.injection.Reflect;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@SuppressModernizer
public class Jsr250Module extends AbstractModule {

    @Override
    public void configure() {
        requireBinding(CloseableInjector.class);
        MyJsr250Destroyer destroyer = new MyJsr250Destroyer();
        requestInjection(destroyer);
        bind(MyJsr250Destroyer.class).toInstance(destroyer);
        bind(Jsr250KeyProvider.class).in(Singleton.class);
        bind(Jsr250PostConstructHandler.class).in(Singleton.class);
        bind(new TypeLiteral<MethodHandler<PreDestroy>>() {
        }).to(Jsr250PreDestroyHandler.class).in(Singleton.class);
        MBinder.wrap(binder()).bindAnnotationInjector(Resource.class, Jsr250KeyProvider.class)
                .handleMethodAfterInjection(PostConstruct.class, Jsr250PostConstructHandler.class);
    }

    static class MyJsr250Destroyer implements InjectorCloseListener {
        @Inject
        Injector injector;

        @Inject
        MethodHandler<PreDestroy> destroyer;

        @Override
        public void onInjectorClosing() {
            Map<Key<?>, Binding<?>> bindings = injector.getAllBindings();
            Multimap<Binding<?>, Binding<?>> dependants = Multimaps.newSetMultimap(
                    new IdentityHashMap<Binding<?>, Collection<Binding<?>>>(), new Supplier<Set<Binding<?>>>() {
                        @Override
                        public Set<Binding<?>> get() {
                            return new HashSet<Binding<?>>();
                        }
                    });
            for (Binding<?> binding : bindings.values()) {
                if (binding instanceof HasDependencies) {
                    for (Dependency<?> dependency : ((HasDependencies) binding).getDependencies()) {
                        if (bindings.containsKey(dependency.getKey())) {
                            dependants.put(injector.getBinding(dependency.getKey()), binding);
                        }
                    }
                }
            }
            Map<Object, Object> done = new IdentityHashMap<Object, Object>(bindings.size());
            for (final Binding<?> binding : bindings.values()) {
                if (Scopes.isSingleton(binding)) {
                    close(binding, done, dependants);
                }
            }
            for (Scope scope : injector.getScopeBindings().values()) {
                preDestroy(scope);
            }
        }

        @SuppressFBWarnings("REC_CATCH_EXCEPTION")
        @SuppressWarnings("illegalCatch")
        private void close(Binding<?> binding, Map<Object, Object> done, Multimap<Binding<?>, Binding<?>> dependants) {
            if (!done.containsKey(binding)) {
                done.put(binding, Void.TYPE);
                for (Binding<?> dependant : dependants.get(binding)) {
                    close(dependant, done, dependants);
                }
                try {
                    if (binding instanceof ProviderInstanceBinding<?>) {
                        Object obj = ((ProviderInstanceBinding) binding).getProviderInstance();
                        if (!done.containsKey(obj)) {
                            preDestroy(obj);
                            done.put(obj, Void.TYPE);
                        }
                    } else if (Scopes.isSingleton(binding)) {
                        Object obj = binding.getProvider().get();
                        if (!done.containsKey(obj)) {
                            preDestroy(obj);
                            done.put(obj, Void.TYPE);
                        }
                    }
                } catch (Exception ex) {
                    // just ignore close errors
                }
            }
        }

        private void preDestroy(Object instance) {
            TypeLiteral<?> type = TypeLiteral.get(Reflect.getTargetClass(instance));
            for (Method method : Reflect.findAllAnnotatedMethods(type.getRawType(), PreDestroy.class)) {
                destroyer.handle(type, instance, method, method.getAnnotation(PreDestroy.class));
            }
        }
    }

}
