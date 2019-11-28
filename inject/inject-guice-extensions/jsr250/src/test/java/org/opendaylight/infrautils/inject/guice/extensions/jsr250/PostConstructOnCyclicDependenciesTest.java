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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.ProvisionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableInjector;
import org.opendaylight.infrautils.inject.guice.extensions.closeable.CloseableModule;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PostConstructOnCyclicDependenciesTest {
    CloseableInjector inj;

    @Before
    public void setUp() throws Exception {
        inj = Guice.createInjector(new Jsr250Module(), new CloseableModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(A.class).to(AImpl.class);
                bind(B.class).to(BImpl.class);
            }
        }).getInstance(CloseableInjector.class);
    }

    @After
    public void tearDown() throws Exception {
        CloseableInjector dying = inj;
        inj = null;
        dying.close();
    }

    @Test
    public void testPostConstructOnCyclicDependency() {
        A obj = null;
        try {
            obj = inj.getInstance(A.class);
        } catch (ProvisionException ex) {
            fail(ex.getMessage());
        }
        assertNotNull(obj);
        assertTrue(obj.hasBeenCalled());
    }
}
