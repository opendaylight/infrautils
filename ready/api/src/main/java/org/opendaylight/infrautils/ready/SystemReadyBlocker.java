/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Service which lets components block "system ready-ness" due to required and
 * ongoing functional or deferred asynchronous initialization.
 *
 * <p>Use of this service is completely optional. Many standard simple components
 * will never need to use this to provide their "ready state". In that case, it
 * will just be determined automatically by their bundles OSGi status as well as
 * Blueprint container initialization. If however your component does things
 * such as e.g. asynchronous background service startup, then it will use this API to
 * inform the system ready-ness tracking to await it.
 *
 * <p>Bundles have to invoke {@link #blockSystemReadyness()} from something
 * like their OSGi <code>BundleActivator</code>'s <code>start()</code>,
 * or a Constructor of a <code>&lt;bean&gt; {@literal @}Singleton</code>,
 * or an <code>{@literal @}PostConstruct init()</code> kind of method.
 * <i>(If they do not, and call this while already in {@link SystemState#Active},
 * then the system ready-ness tracker has no way of knowing before hand that
 * there are still components to await, and will be brittle and flaky-ly
 * "stutter" back and forth between system states.)</i>
 *
 * <p>This service is not specific to OSGi per se, and can be used by
 * components which can run in plain Java environments as well.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
public interface SystemReadyBlocker {

    SystemReadyFeedback blockSystemReadyness();

    interface SystemReadyFeedback {
        void readyNow();
    }

}
