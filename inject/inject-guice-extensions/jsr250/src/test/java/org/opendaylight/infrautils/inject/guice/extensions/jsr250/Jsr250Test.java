/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;
import org.opendaylight.infrautils.inject.guice.extensions.injection.MBinder;
import org.opendaylight.infrautils.inject.guice.extensions.injection.Reflect;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public class Jsr250Test {

    @Test
    public void test_resource_with_type() throws Exception {
        Guice.createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule()).getInstance(Res1Class.class);
        assertEquals(2, Res1Class.verified);
    }

    static class Res1Class {
        static int verified;

        @Resource
        Injector injector;

        @Resource
        Provider<Injector> provider;

        @Resource
        void init(AA aa) {
            // field injection is done before method injection
            assertNotNull(injector);
            assertNotNull(provider);
            assertNotNull(aa);
            verified++;
        }

        @PostConstruct
        void init() {
            assertNotNull(injector);
            assertNotNull(provider);
            assertSame(injector, provider.get());
            verified++;
        }
    }

    @Test
    public void test_resource_with_name() throws Exception {
        final AA aa1 = new AA();
        final AA aa2 = new AA();
        Res2Class res2 = Guice
                .createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(AA.class).annotatedWith(Names.named("aa1")).toInstance(aa1);
                        bind(AA.class).annotatedWith(Names.named("aa2")).toInstance(aa2);
                    }
                }).getInstance(Res2Class.class);
        assertEquals(aa1, res2.aa1);
        assertEquals(aa2, res2.aa2);
        assertTrue(res2.aa1 != res2.aa2);
    }

    static class Res2Class {
        @Resource
        AA aa1;

        @Resource
        AA aa2;
    }

    @Test
    public void test_post_inject_param() throws Exception {
        assertFalse(MyM.AAA.CALLED);
        assertFalse(MyM.AAA.SECOND);
        Guice.createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule(), new MyM());
        assertTrue(MyM.AAA.CALLED);
        assertTrue(MyM.AAA.SECOND);
    }

    static class MyM extends AbstractModule {
        @Override
        protected void configure() {
            bind(AAA.class);
        }

        @Singleton
        static class AAA {
            static boolean CALLED;
            static boolean SECOND;

            @Inject
            BBB bbb;

            @PostConstruct
            void init() {
                SECOND = true;
                assertNotNull(bbb);
            }

            @PostConstruct
            void init(BBB bb) {
                assertNotNull(bb);
                assertNotNull(bbb);
                CALLED = true;
            }
        }
    }

    @Singleton
    static class BBB {
    }

    @Test
    public void test_destroy() throws Exception {
        final Class[] cc = {AA.class};
        CloseableInjector injector = Guice
                .createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        for (Class<?> c : cc) {
                            bind(c);
                        }
                        // just for fun
                        MBinder.wrap(binder()).bindInterceptor(Matchers.subclassesOf(Base.class), Matchers.any(),
                                new MethodInterceptor() {
                                    @Override
                                    public Object invoke(MethodInvocation invocation) throws Throwable {
                                        return invocation.proceed();
                                    }
                                });
                    }
                }).getInstance(CloseableInjector.class);
        for (Class<?> c : cc) {
            injector.getInstance(c);
            injector.getInstance(c);
        }

        Collections.sort(Base.CALLS);
        assertEquals("[]", Base.CALLS.toString());

        for (Class<?> c : cc) {
            injector.getInstance(c);
        }

        injector.close();

        Collections.sort(Base.CALLS);
        assertEquals("[AA]", Base.CALLS.toString());
    }

    static class Base {
        static final List<String> CALLS = new ArrayList<String>();

        @PreDestroy
        void close() {
            CALLS.add(Reflect.getTargetClass(getClass()).getSimpleName());
        }
    }

    @Singleton
    static class AA extends Base {
    }

    @Test
    public void test_inject_in_interceptor() throws Exception {
        B.calls.clear();
        CloseableInjector injector = Guice
                .createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        MBinder.wrap(binder()).bindInterceptor(Matchers.subclassesOf(A.class), Matchers.any(),
                                new MethodInterceptor() {
                                    @Resource
                                    Injector injector;

                                    @Override
                                    public Object invoke(MethodInvocation invocation) throws Throwable {
                                        assertNotNull(injector);
                                        return invocation.proceed();
                                    }
                                });
                    }
                }).getInstance(CloseableInjector.class);
        B obj = injector.getInstance(B.class);
        assertSame(obj, injector.getInstance(B.class));
        obj.intercept();
        injector.close();
        assertEquals("[1, 2, 3]", B.calls.toString());
    }

    @Test
    public void test() throws Exception {
        B.calls.clear();
        CloseableInjector injector = Guice
                .createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                }).getInstance(CloseableInjector.class);
        injector.getInstance(B.class);
        assertEquals("[1, 2]", B.calls.toString());
        injector.close();
        assertEquals("[1, 2, 3]", B.calls.toString());
    }

    static class A {
        static List<Integer> calls = new LinkedList<Integer>();

        @Inject
        void method(B obj) {
            calls.add(1);
        }

        @PostConstruct
        void init() {
            calls.add(2);
        }

        @PreDestroy
        void close() {
            calls.add(3);
        }
    }

    @Singleton
    static class B extends A {
        void intercept() {
        }
    }

}
