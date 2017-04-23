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
 * also similar to the <a href=
 * "http://felix.apache.org/documentation/subprojects/apache-felix-event-admin.html">OSGi
 * Event Admin API</a> (which has a loose Event with Properties instead of
 * strong types), and
 * <a href="https://github.com/google/guava/wiki/EventBusExplained">Guava's
 * EventBus</a> (who's API is relatively similar but has some differences), or
 * the <a href=
 * "https://docs.spongepowered.org/stable/en/plugin/event/listeners.html">Events
 * in the Sponge powered Minecraft modding API</a>.
 *
 * @author Michael Vorburger.ch
 */
@org.eclipse.jdt.annotation.NonNullByDefault
package org.opendaylight.infrautils.events;
