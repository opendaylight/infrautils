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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class Jsr250LifecycleTest {

    public abstract static class LifecycleBase {
        public String startSequence = "";
        public String stopSequence = "";
    }

    @Singleton
    public static class LifecycleSimple extends LifecycleBase {
        @PostConstruct
        public void init() {
            startSequence += "A";
        }

        @PreDestroy
        public void destroy() {
            stopSequence += "A";
        }
    }

    @Singleton
    public static class LifecycleMultiple extends LifecycleBase {
        @PostConstruct
        public void init() {
            startSequence += "A";
        }

        @PostConstruct
        public void init2() {
            startSequence += "B";
        }

        @PreDestroy
        public void destroy() {
            stopSequence += "A";
        }

        @PreDestroy
        public void destroy2() {
            stopSequence += "B";
        }
    }

    @Singleton
    public static class LifecycleExtends extends LifecycleSimple {
        @PostConstruct
        public void init2() {
            startSequence += "X";
        }

        @PreDestroy
        public void destroy2() {
            stopSequence += "X";
        }
    }

    @Singleton
    public static class LifecycleOverrides extends LifecycleSimple {
        @Override
        @PostConstruct
        public void init() {
            startSequence += "B";
        }

        @Override
        @PreDestroy
        public void destroy() {
            stopSequence += "B";
        }
    }

    @Singleton
    public static class LifecycleOverridesRemovesAnnotations extends LifecycleOverrides {
        @Override
        public void init() {
            startSequence += "C";
        }

        @Override
        public void destroy() {
            stopSequence += "C";
        }
    }

    @Singleton
    public static class LifecyclePrivateMethods extends LifecycleBase {
        @PostConstruct
        private void init() {
            startSequence += "D";
        }

        @PreDestroy
        private void destroy() {
            stopSequence += "D";
        }
    }

    @Singleton
    public static class LifecycleSameNamePrivateMethods extends LifecyclePrivateMethods {
        @PostConstruct
        private void init() {
            startSequence += "E";
        }

        @PreDestroy
        private void destroy() {
            stopSequence += "E";
        }
    }

    @Test
    public void testLifecycle() {
        assertLifeCycleSequence(LifecycleSimple.class, "A", "A");
        assertLifeCycleSequence(LifecycleExtends.class, "AX", "XA");
        assertLifeCycleSequence(LifecycleOverrides.class, "B", "B");
        assertLifeCycleSequence(LifecycleOverridesRemovesAnnotations.class, "", "");
        assertLifeCycleSequence(LifecyclePrivateMethods.class, "D", "D");
        // TODO: verify if private methodes of super classes should be invoked before
        // subclasses in case of @PostConstruct
        // assertLifeCycleSequence(LifecycleSameNamePrivateMethods.class, "ED", "DE");
        assertLifeCycleSequence(LifecycleSameNamePrivateMethods.class, "DE", "ED");

        // order among methods in same class is undefined so we just test that all of them were called
        assertLifeCycleSequenceContainsAll(LifecycleMultiple.class, "AB", "BA");
    }

    private void assertLifeCycleSequenceContainsAll(final Class<? extends LifecycleBase> clazz, String startSequence,
            String endSequence) {
        CloseableInjector injector = createInjector(clazz);
        LifecycleBase component = injector.getInstance(clazz);
        assertContainsAll(component.startSequence, startSequence);
        injector.close();
        assertContainsAll(component.stopSequence, endSequence);
    }

    private void assertLifeCycleSequence(final Class<? extends LifecycleBase> clazz, String expectedStartSequence,
            String expectedStopSequence) {
        CloseableInjector injector = createInjector(clazz);
        LifecycleBase component = injector.getInstance(clazz);
        assertEquals(expectedStartSequence, component.startSequence);
        injector.close();
        assertEquals(expectedStopSequence, component.stopSequence);
    }

    private CloseableInjector createInjector(final Class<? extends LifecycleBase> clazz) {
        return Guice.createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(clazz);
            }
        }).getInstance(CloseableInjector.class);
    }

    private void assertContainsAll(String string, String requiredCharacters) {
        for (char ch : requiredCharacters.toCharArray()) {
            if (string.indexOf(ch) == -1) {
                fail("String [" + string + "] does not contain character [" + ch + "]");
            }
        }
    }
}

