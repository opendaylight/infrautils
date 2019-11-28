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
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;
import org.opendaylight.infrautils.inject.guice.extensions.injection.KeyProviderSkeleton;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MBinder;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public class BinderHelperTest {

    @Autowire
    CloseableInjector jsr250Injector;

    @Test
    public void test() throws Exception {
        assertNull(jsr250Injector);
        Guice.createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
            @Override
            protected void configure() {
                MBinder.wrap(binder()).bindAnnotationInjector(Autowire.class, AutowireKeyProvider.class);
                requestInjection(BinderHelperTest.this);
            }
        });
        assertNotNull(jsr250Injector);
    }

    @Target({METHOD, CONSTRUCTOR, FIELD})
    @Retention(RUNTIME)
    static @interface Autowire {
        String value() default "";
    }

    static class AutowireKeyProvider extends KeyProviderSkeleton<Autowire> {
        @Override
        public Key<?> getKey(TypeLiteral<?> injectedType, Field injectedMember, Autowire resourceAnnotation) {
            String name = resourceAnnotation.value();
            return name.length() == 0 ? super.getKey(injectedType, injectedMember, resourceAnnotation)
                    : Key.get(injectedType.getFieldType(injectedMember), Names.named(name));
        }
    }

}
