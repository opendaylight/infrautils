/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.ready.internal;

import javax.annotation.Nullable;
import org.opendaylight.infrautils.ready.order.FunctionalityReady;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyNotifier;
import org.opendaylight.infrautils.ready.order.FunctionalityReadyRegistration;

/**
 * Implementation of {@link FunctionalityReadyNotifier}.
 *
 * @author Michael Vorburger.ch
 */
public class FunctionalityReadyNotifierImpl implements FunctionalityReadyNotifier {

    @Override
    @Nullable
    public FunctionalityReadyRegistration<? extends FunctionalityReady>
        register(Class<? extends FunctionalityReady> markerInterface) {
        // TODO, incl. TDD IT
        //   * check argument that it's an interface and not a class
        //   * check OSGi registry for previously registered duplicates (allow only 1)
        //   * create new instance from interface not class
        //   * OSGi registry register it
        //   * INFO log it
        return null;
    }

}
