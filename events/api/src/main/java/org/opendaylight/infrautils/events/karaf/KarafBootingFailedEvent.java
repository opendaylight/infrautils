/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.karaf;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
// TODO @OpendaylightStyle <<: /** Gangnam **/
@Value.Style(visibility = ImplementationVisibility.PRIVATE /*, stagedBuilder = true */)
public interface KarafBootingFailedEvent extends KarafBootingEvent {

    String shortSummary();

    String longDetails();

}
