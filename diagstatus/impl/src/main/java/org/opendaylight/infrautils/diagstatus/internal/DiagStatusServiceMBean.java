/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import java.util.Map;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;

public interface DiagStatusServiceMBean {

    String acquireServiceStatus();

    String acquireServiceStatusDetailed();

    String acquireServiceStatusBrief();

    String acquireServiceStatusAsJSON(String outputType);

    // TODO Why do we need the same in a Map here which we can already get as a Collection from DiagStatusService?
    // Does the DiagStatusService need to return a Map instead of a Collection?
    Map<String, ServiceDescriptor> acquireServiceStatusMap();
}
