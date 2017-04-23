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
 * EventBus</a>, but which 1. only offers annotation based subscription not both
 * annotation as well as simply inline lambda listeners like this API, 2. where
 * {@link EventPublisher#publish(Object)} does not return a Future as here which is useful for testing, and
 * which 3. does not support
 * {@link EventSubscriber#subscribe(com.google.common.reflect.TypeToken, EventListener)}
 * subscribing to re-ified types;
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
