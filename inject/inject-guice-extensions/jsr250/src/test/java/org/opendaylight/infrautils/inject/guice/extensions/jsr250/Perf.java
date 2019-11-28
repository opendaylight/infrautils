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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import javax.annotation.PostConstruct;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com) date 2013-07-06
 */
final class Perf {

    private Perf() {
    }

    private static long invocations;

    public static class TestClassWithPostConstruct {

        public void method1() {
        }

        public void method2() {
        }

        @PostConstruct
        public void method3() {
            invocations++;
        }

        public void method4() {
        }
    }

    public static class TestClassWithoutPostConstruct {

        public void method1() {
        }

        public void method2() {
        }

        public void method3() {
        }

        public void method4() {
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // connect visual vm
        Thread.sleep(10000);

        int count = 1 * 1000 * 10000;

        time("without Mycila", count, createSimpleInjector(), TestClassWithPostConstruct.class);
        time("with Mycila", count, createInjectorWithMycila(), TestClassWithPostConstruct.class);

        time("without Mycila", count, createSimpleInjector(), TestClassWithoutPostConstruct.class);
        time("with Mycila", count, createInjectorWithMycila(), TestClassWithoutPostConstruct.class);
    }

    private static void time(String name, int count, Injector injector, Class<?> clazz) {

        // warm up
        for (int i = 0; i < 1000 * 1000; i++) {
            injector.getInstance(clazz);
        }

        invocations = 0;
        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            injector.getInstance(clazz);
        }

        long end = System.currentTimeMillis();
    }

    private static Injector createInjectorWithMycila() {
        return Guice.createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule());
    }

    private static Injector createSimpleInjector() {
        return Guice.createInjector(Stage.PRODUCTION);
    }
}
