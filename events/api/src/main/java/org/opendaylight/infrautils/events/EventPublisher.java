/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes (AKA "posts" or "sends") event messages.
 *
 * @author Michael Vorburger.ch
 */
public interface EventPublisher {

    /**
     * Publish an event asynchronously.
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

}
