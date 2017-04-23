/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import com.google.common.reflect.TypeToken;

/**
 * Subscribes to (AKA "registers for") event messages.
 *
 * @author Michael Vorburger.ch
 */
public interface EventSubscriber extends AutoCloseable {

    /**
     * Subscribe an event listener by specifying an event Class type.
     *
     * @param eventClass
     *            Event type as java.lang.Class
     * @param listener
     *            Event listener
     *
     * @return Subscription used (only) to {@link #unsubscribe(Subscription)}
     */
    <E> Subscription subscribe(Class<E> eventClass, EventListener<? super E> listener);

    /**
     * Subscribe an event listener by specifying an event TypeToken. This is
     * useful to support "reified" event listeners using generics.
     *
     * @param eventClass
     *            Event type as TypeToken (e.g.
     *            <code>new TypeToken&lt;ServiceRegisteredEvent&lt;YourService&gt;&gt;() {}</code>)
     * @param listener
     *            Event listener
     *
     * @return Subscription used (only) to {@link #unsubscribe(Subscription)}
     */
    <E> Subscription subscribe(TypeToken<E> reifiedEventClass, EventListener<E> listener);
    // TODO how to do <? super E> instead of <E> and get
    // ReifiedEventsTest.testSubscribeSuperTypeListener() to work?!

    /**
     * Registers all methods annotated with {@link Listener} on {@code object}
     * to receive events.
     *
     * @param object
     *            object whose onXYZ() listener methods should be registered.
     *
     * @return Subscription used (only) to {@link #unsubscribe(Subscription)}
     *
     * @throws IllegalArgumentException
     *             if object has no methods annotated with {@link Listener}, or
     *             if any of these methods does not accept exactly one argument.
     */
    Subscription subscribe(Object object) throws IllegalArgumentException;

    void unsubscribe(Subscription subscription) throws IllegalArgumentException;

    interface Subscription {
    }
}
