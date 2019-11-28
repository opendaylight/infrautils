/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice;

import com.google.inject.AbstractModule;
import javax.annotation.PreDestroy;

/**
 * Guice wiring module which can do stuff on close of Injector.
 * This is typically used in an injector which explicitly creates legacy types
 * which have close methods which need to be explicitly called.  It is easier
 * and better to just annotate such types with PreDestroy, but if you cannot,
 * this is useful.
 *
 * <p>This works in conjunction with org.opendaylight.infrautils.simple.Main
 * or org.opendaylight.infrautils.inject.guice.testutils.GuiceRule, both of
 * which explicitly use com.mycila.guice.ext.closeable.CloseableInjector.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractCloseableModule extends AbstractModule {

    private static class Closer {
        private final AbstractCloseableModule closeableModule;

        Closer(AbstractCloseableModule closeableModule) {
            this.closeableModule = closeableModule;
        }

        @PreDestroy
        void close() throws Exception {
            closeableModule.close();
        }
    }

    @Override protected final void configure() {
        configureCloseables();
        bind(Closer.class).toInstance(new Closer(this));
    }

    protected abstract void configureCloseables();

    @PreDestroy
    public abstract void close() throws Exception;

}
