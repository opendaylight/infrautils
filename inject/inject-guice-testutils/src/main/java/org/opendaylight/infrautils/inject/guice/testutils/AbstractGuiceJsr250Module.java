/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils;

import com.google.inject.AbstractModule;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import org.opendaylight.infrautils.inject.ModuleSetupRuntimeException;

/**
 * Guice module with built-in Mycila Guice Extensions for JSR-250 &amp;
 * Closeable support for {@literal @}PreDestroy &amp; {@literal @}PostConstruct.
 *
 * @author Michael Vorburger
 */
public abstract class AbstractGuiceJsr250Module extends AbstractModule {

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected final void configure() throws ModuleSetupRuntimeException {
        install(new CloseableModule());
        install(new Jsr250Module());
        try {
            configureBindings();
        } catch (Exception e) {
            throw new ModuleSetupRuntimeException(e);
        }
    }

    protected abstract void configureBindings() throws Exception;

    /**
     * Binds instance to both the interfaceClass as well as the implementationClass.
     *
     * @param interfaceClass class type of an interface
     * @param implementationClass  class type of implementing class
     * @param instance an instance implementing both interfaceClass & implementationClass
     */
    @SuppressWarnings("unchecked")
    protected <T> void bind2ToInstance(Class<T> interfaceClass, Class<? extends T> implementationClass, T instance) {
        if (implementationClass.equals(interfaceClass)) {
            throw new IllegalArgumentException("interfaceClass should not be the same as implementationClass: "
                    + interfaceClass + "; " + implementationClass);
        }
        bind(interfaceClass).toInstance(instance);
        bind((Class<T>) implementationClass).toInstance(instance);
    }

}
