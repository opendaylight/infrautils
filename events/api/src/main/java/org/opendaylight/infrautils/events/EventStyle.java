/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

/**
 * An Immutables.org gangnam style, useful for generating event implementations.
 *
 * @author Michael Vorburger.ch
 */
@Value.Style(visibility = ImplementationVisibility.SAME,
             allParameters = true, typeImmutable = "*Impl")
public @interface EventStyle { }
