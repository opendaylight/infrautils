/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Events.
 *
 * This API is, obviously, inspired by the well-known "pub/sub" pattern. It is
 * also similar yet different to:
 * <ul>
 *
 * <li><a href=
 * "http://felix.apache.org/documentation/subprojects/apache-felix-event-admin.html">OSGi's
 * Event Admin API</a>, which has a loose Event with Properties instead of
 * strong types;
 * <li><a href="https://github.com/google/guava/wiki/EventBusExplained">Guava's
 * EventBus</a>, but:<ul>
 * <li>{@link EventPublisher#publish(Object)} does not
 * return a {@link java.util.concurrent.CompletableFuture} as this does, which is very
 * useful e.g. for testing,
 * <li>which use a cache of java.lang.Class in a static, which is not
 * good in a dynamic class loading environment like OSGi,
 * <li>assumes subscribed listener
 * methods are thread-safe (no AllowConcurrentEvents, just
 * {@link Listener#isThreadSafe()})
 * <li>fails on
 * {@link EventSubscriber#subscribe(Object)} if you mistakingly pass the wrong
 * object which has no suitable annotations or has a non-void return value
 * <li><i>TODO not yet implemented: only offers annotation based subscription not both
 * annotation as well as simply inline lambda listeners like this API (including
 * subscribing to re-ified types);</i>
 * </ul>
 * <li><a href=
 * "https://docs.spongepowered.org/stable/en/plugin/event/listeners.html">Events
 * in the Sponge powered Minecraft modding API</a>;
 * <li><a href="http://doc.akka.io/docs/akka/current/java/event-bus.html">Akka's
 * Event Bus</a>.
 * </ul>
 *
 * @author Michael Vorburger.ch
 */
@org.eclipse.jdt.annotation.NonNullByDefault
package org.opendaylight.infrautils.events;
