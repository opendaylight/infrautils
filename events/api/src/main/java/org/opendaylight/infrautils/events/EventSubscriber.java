/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

/**
 * Subscribes to (AKA "registers for") event messages.
 *
 * @author Michael Vorburger.ch
 */
public interface EventSubscriber {

    /**
     * Subscribe.
     * @param <E> Even type (note that there is no Event base type / marker interface)
     * @param eventClass Event type
     * @param listener Event listener
     * @return Subscription used (only) to {@link #unsubscribe(Subscription)}
     */
    <E> Subscription subscribe(Class<E> eventClass, EventListener<? super E> listener);

    /**
     * Registers all methods annotated with {@link Listener} on {@code object} to receive events.
     *
     * @param object object whose subscriber methods should be registered.
     */
    Subscription subscribe(Object object) throws IllegalArgumentException;

    void unsubscribe(Subscription subscription) throws IllegalArgumentException;

    interface Subscription { }
}
