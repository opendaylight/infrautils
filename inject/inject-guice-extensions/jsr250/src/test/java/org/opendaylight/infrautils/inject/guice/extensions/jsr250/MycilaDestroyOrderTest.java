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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class MycilaDestroyOrderTest {

    @Singleton
    public static class Repository {
        private boolean closed = false;

        @PreDestroy
        public void close() {
            closed = true;
        }

        public void writeApplicationStatus(String message) {
            if (closed) {
                throw new IllegalStateException("Repository closed!");
            }
        }
    }

    @Singleton
    public static class Service {
        @Inject
        private Repository repository;

        @PreDestroy
        public void destroy() {
            repository.writeApplicationStatus("Closing application");
        }
    }

    @Test
    public void testDestroyOrder() {

        // This test fails because Mycila destroys singletons in the order they are bound

        CloseableInjector injector = Guice
                .createInjector(Stage.PRODUCTION, new Jsr250Module(), new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Repository.class);
                        bind(Service.class);
                    }
                }).getInstance(CloseableInjector.class);

        injector.close();
    }
}
