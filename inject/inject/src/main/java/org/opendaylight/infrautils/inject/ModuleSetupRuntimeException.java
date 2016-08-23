/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.inject;

/**
 * Exception to throw from a static Dependency Inject Framework's "Wiring" class.
 *
 * <p>Also used in {@link Lifecycle#init()} and {@link Lifecycle#destroy()}, because
 * those methods are typically called from DI's Wiring classes (typically implicitly
 * by a DI framework, and not hand-written Wiring code).
 *
 * <p>For example, throw this from methods in a class implementing Guice's Module
 * interface, or from methods annotated with Dagger's @Provides in a @Module
 * class, which <i>"may only throw unchecked exceptions"</i>.  In particular, when you
 * have to catch checked exceptions while creating objects, wrap them into this
 * unchecked Exception.
 *
 * <p>When you use this Exception in a Dagger/Guice/etc. Module, you should
 * probably write a simple test for the Module, just to verify it (alone) works
 * at run-time (if there is a checked exception to catch, it probably
 * initializes something that is non-trivial and could fail; so best to have a
 * non-regression test for that Module).
 *
 * @author Michael Vorburger
 */
public class ModuleSetupRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1795982967617796415L;

    public ModuleSetupRuntimeException(Exception cause) {
        super(cause);
    }
}
