/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Event Bus which lets users publish (AKA "posts" or "sends") event messages,
 * and subscribe (AKA "registers") listeners for such event messages.

 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface EventBus extends AutoCloseable {

    /**
     * Publish an event and asynchronously and concurrently notifies all
     * listeners currently subscribed for that event type.
     *
     * <p>This normally returns quickly, never blocks (possibly at the expense of
     * more memory use to store unprocessed events), but may return an immediate
     * failed future in case of a failure due to an overloaded (or closed) event
     * bus, but never directly throws {@link RejectedExecutionException} to the
     * caller. Any exceptions thrown by subscribers are also (eventually, not
     * immediately) returned via the future as exceptional completions.
     *
     * @param event
     *            Event (type of the event is, intentionally, simply Object)
     *
     * @return Future useful for taking deferred action after all subscribed
     *         listeners processed event, or for reacting to exceptions (it will
     *         already have been error logged, so no need to use to only log)
     */
    CompletableFuture<Void> publish(Object event);

    // TODO document the dead event behavior?
    // <p/>If there are no listeners registered for the event's type, then the
    // message is queued if the system is still booting, but just discarded with
    // a WARN log if we're fully up-and-running.

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
//   <E> Subscription subscribe(Class<E> eventClass, EventListener<? super E> listener);

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
//   <E> Subscription subscribe(TypeToken<E> reifiedEventClass, EventListener<E> listener);
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
