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

import static org.junit.Assert.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provider;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@SuppressModernizer
public class MycilaInitDestroyBindingsTest {

    public static class InitDestroyCounter {

        public int initialized;
        public int destroyed;

        @PostConstruct
        public void init() {
            initialized++;
        }

        @PreDestroy
        public void destroy() {
            destroyed++;
        }
    }

    public interface SomeInterface {
    }

    public interface AnotherInterface {
    }

    public static class NonScopedTestObject extends InitDestroyCounter implements SomeInterface, AnotherInterface {
    }

    @Singleton
    public static class TestSingleton extends InitDestroyCounter implements SomeInterface, AnotherInterface {
    }

    public abstract static class TestProvider<T> extends InitDestroyCounter implements Provider<T> {
    }

    @Test
    public void testUntargettedBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(TestSingleton.class);
                    }
                }).getInstance(CloseableInjector.class);

        TestSingleton component = injector.getInstance(TestSingleton.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);
    }

    @Test
    public void testLinkedBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(SomeInterface.class).to(TestSingleton.class);
                    }
                }).getInstance(CloseableInjector.class);

        TestSingleton component = (TestSingleton) injector.getInstance(SomeInterface.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);

        // Guice creates an internal jit ConstructorBinding therefor this fails as Mycila destroys on both bindings
    }

    @Test
    public void testLinkedAndUntargettedBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(TestSingleton.class);
                        bind(SomeInterface.class).to(TestSingleton.class);
                    }
                }).getInstance(CloseableInjector.class);

        TestSingleton component = (TestSingleton) injector.getInstance(SomeInterface.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);

        // Both bindings return the same object therefor this fails as Mycila destroys on both bindings
    }

    @Test
    public void testInstanceBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        TestSingleton instance = new TestSingleton();
                        bind(SomeInterface.class).toInstance(instance);
                        bind(AnotherInterface.class).toInstance(instance);
                    }
                }).getInstance(CloseableInjector.class);

        TestSingleton component = (TestSingleton) injector.getInstance(SomeInterface.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);

        // Both bindings return the same object therefor this fails as Mycila destroys on both bindings
    }

    @Test
    public void testNonSingletonInstanceBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        NonScopedTestObject instance = new NonScopedTestObject();
                        bind(SomeInterface.class).toInstance(instance);
                        bind(AnotherInterface.class).toInstance(instance);
                    }
                }).getInstance(CloseableInjector.class);

        // The object is not scoped but Guice considers instance bindings to be singletons so Mycila will destroy it

        NonScopedTestObject component = (NonScopedTestObject) injector.getInstance(SomeInterface.class);
        assertEquals(1, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(1, component.initialized);
        assertEquals(1, component.destroyed);

        // Both bindings return the same object therefor this fails as Mycila destroys on both bindings
    }

    @Test
    public void testProviderBinding() {
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        final TestSingleton instance = new TestSingleton();
                        bind(TestSingleton.class).toProvider(new Provider<TestSingleton>() {
                            @Override
                            public TestSingleton get() {
                                return instance;
                            }
                        });
                        bind(SomeInterface.class).toProvider(new Provider<SomeInterface>() {
                            @Override
                            public TestSingleton get() {
                                return instance;
                            }
                        });
                    }
                }).getInstance(CloseableInjector.class);

        // Provider bindings are not scoped so Mycila will not destroy them

        TestSingleton component = (TestSingleton) injector.getInstance(SomeInterface.class);
        assertEquals(0, component.initialized);
        assertEquals(0, component.destroyed);

        injector.close();
        assertEquals(0, component.initialized);
        assertEquals(0, component.destroyed);
    }

    @Test
    public void testProvider() {

        final TestProvider<TestSingleton> testProvider1 = new TestProvider<TestSingleton>() {
            @Override
            public TestSingleton get() {
                return new TestSingleton();
            }
        };
        final TestProvider<SomeInterface> testProvider2 = new TestProvider<SomeInterface>() {
            @Override
            public TestSingleton get() {
                return new TestSingleton();
            }
        };

        final CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(TestSingleton.class).toProvider(testProvider1);
                        bind(SomeInterface.class).toProvider(testProvider2).in(Singleton.class);
                    }
                }).getInstance(CloseableInjector.class);

        // Guice will do injection on the provider but will not destroy it since its not singleton scoped

        assertEquals(1, testProvider1.initialized);
        assertEquals(0, testProvider1.destroyed);
        assertEquals(1, testProvider2.initialized);
        assertEquals(0, testProvider2.destroyed);

        injector.close();

        assertEquals(1, testProvider1.initialized);
        assertEquals(0, testProvider1.destroyed);
        assertEquals(1, testProvider2.initialized);
        assertEquals(1, testProvider2.destroyed);
    }
}
