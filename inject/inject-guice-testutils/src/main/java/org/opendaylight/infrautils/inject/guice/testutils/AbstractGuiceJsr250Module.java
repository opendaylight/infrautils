/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.testutils;

/**
 * Convenience Guice module support class, which installs the
 * {@link AnnotationsModule}, and handles exceptions as the
 * {@link AbstractCheckedModule} does.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractGuiceJsr250Module extends AbstractCheckedModule {

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected final void checkedConfigure() throws Exception {
        install(new AnnotationsModule());
        configureBindings();
    }

    protected abstract void configureBindings() throws Exception;

    /**
     * Binds instance to both the interfaceClass as well as the implementationClass.
     *
     * @param interfaceClass class type of an interface
     * @param implementationClass class type of implementing class
     * @param instance an instance implementing both interfaceClass &amp; implementationClass
     * @param <T> type of interfaceClass
     */
    @SuppressWarnings("unchecked")
    protected <T> void bindTypesToInstance(Class<T> interfaceClass, Class<? extends T> implementationClass,
            T instance) {
        if (implementationClass.equals(interfaceClass)) {
            throw new IllegalArgumentException("interfaceClass should not be the same as implementationClass: "
                    + interfaceClass + "; " + implementationClass);
        }
        bind(interfaceClass).toInstance(instance);
        bind((Class<T>) implementationClass).toInstance(instance);
    }

}
