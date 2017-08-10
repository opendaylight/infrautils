/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready;

import org.opendaylight.infrautils.ready.order.FunctionalityReady;

/**
 * Functionality ready marker OSGi service indicating that all "boot features"
 * have successfully loaded.
 *
 * <p>This is the {@link FunctionalityReady} equivalent of the
 * {@link SystemReadyListener#onSystemBootReady()} callback.
 *
 * @author Michael Vorburger.ch
 */
public interface BundlesBootedReady extends FunctionalityReady { }
