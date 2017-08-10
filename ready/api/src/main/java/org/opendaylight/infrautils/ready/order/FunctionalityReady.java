/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.order;

/**
 * Marker interface for ready functionality.
 * Implemented by interfaces used purely to “signal” e.g.
 * “state changes” during boot up for specific “functionality” offered by a
 * bundle. Another bundle can then await the respective “boot event” by
 * declaring a regular OSGi service dependency to it (using e.g.Blueprint BP, or
 * Declarative Services DS). This allows to order the boot sequence.
 *
 * <p>BP (or DS) of course already implicitly do service initialization ordering
 * according to the dependency graph (but beware of BP's use of java.lang.reflect.Proxy).
 * This infrastructure is therefore useful only if:<ol>
 *
 * <li>there is no natural service interface, or
 *
 * <li>a service does further more heavy weight initialization asynchronously
 * in a background thread in order not to block e.g. Blueprint, and wants to
 * signal when that background initialisation is really ready to anything that
 * must be specifically aware of that,
 *
 * <li>you need more “fine grained” signaling than the availability of your main service interface/s can
 * indicate.
 * </ol>
 *
 * <p>Some examples where this is useful include: <ul>
 * <li>Infrautils’ own system ready service signaling that all features have booted all bundles
 * without errors and all blueprint initialization is completed (similar to
 * Karaf own org.apache.karaf.features.BootFinished),
 * <li>Daexim’s import on boot completed successfully.
 * </ul>
 *
 * <p>Interfaces extending this one typically have no methods. By convention, they are named
 * with a *Ready suffix.
 *
 * <p>This is just a convenience maker interface. One could
 * well just use the “pattern” proposed by this without implementing this
 * interface, nor using {@link FunctionalityReadyNotifier}. However, using this gives a
 * clear indication that a “fake service” has this purpose, and allows one to
 * find them all by type, as well as directly refer to this documentation.
 *
 * @see FunctionalityReadyNotifier
 *
 * @author Michael Vorburger.ch
 */
public interface FunctionalityReady { }
