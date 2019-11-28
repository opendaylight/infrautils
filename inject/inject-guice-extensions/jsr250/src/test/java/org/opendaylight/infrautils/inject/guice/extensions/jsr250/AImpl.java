/*
 * Copyright (C) 2010 Mycila (mathieu.carbou@gmail.com).  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject.guice.extensions.jsr250;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * This code originated in https://github.com/mycila/guice and was forked into
 * OpenDaylight.
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class AImpl implements A {
    //@Inject
    private Provider<B> other;
    private boolean called = false;

    @Inject
    public AImpl(Provider<B> obj) {
        this.other = obj;
    }

    @PostConstruct
    public void init() {
        other.get().callB();
    }

    @Override
    public void callA() {
        called = true;
    }

    public boolean hasBeenCalled() {
        return called;
    }
}
