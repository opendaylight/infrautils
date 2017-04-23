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
 * Publishes (AKA "posts" or "sends") event messages.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface EventPublisher extends AutoCloseable {

    /**
     * Publish an event and asynchronously and concurrently notifies all
     * listeners currently subscribed for that event type.
     *
     * <p>
     * This normally returns quickly, never blocks (possibly at the expense of
     * more memory use to store unprocessed events), but may return an immediate
     * failed future in case of a failure due to an overloaded (or closed) event
     * bus, but never directly throws {@link RejectedExecutionException} to the
     * caller. Any exceptions thrown by subscribers are also (eventually, not immediately)
     * returned via the future as exceptional completions.
     *
     * @param event
     *            Event (Note that the type of the event is, intentionally,
     *            simply Object.)
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

}
