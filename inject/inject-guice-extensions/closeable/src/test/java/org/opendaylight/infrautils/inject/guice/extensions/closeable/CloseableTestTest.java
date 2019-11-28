/**
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.closeable;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;

import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This code originated in https://github.com/mycila/guice and was forked into OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com) date 2013-07-21
 */
@RunWith(JUnit4.class)
public final class CloseableTestTest {

    @Test
    public void test() throws Exception {
        CloseableInjector injector = Guice
                .createInjector(Stage.PRODUCTION, new CloseableModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MustClose.class).in(Singleton.class);
                    }
                }).getInstance(CloseableInjector.class);
        Assert.assertEquals(0, MustClose.hits);
        injector.close();
        Assert.assertEquals(1, MustClose.hits);
    }

    static class MustClose implements InjectorCloseListener {
        static int hits;

        @Override
        public void onInjectorClosing() {
            hits++;
        }
    }
}
