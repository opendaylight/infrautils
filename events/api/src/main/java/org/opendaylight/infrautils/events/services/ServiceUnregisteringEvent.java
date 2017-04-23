/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.services;

import org.immutables.value.Value;
import org.opendaylight.infrautils.events.EventStyle;

@EventStyle
@Value.Immutable
public interface ServiceUnregisteringEvent<T> extends ServiceEvent<T> {
}

