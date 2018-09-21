/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.util.Map;

/**
 * DiagStatus MBean Operations.
 *
 * @author Faseela K
 */
public interface DiagStatusServiceMBean {

    int RMI_REGISTRY_PORT = 6886;

    String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";

    String acquireServiceStatus();

    String acquireServiceStatusDetailed();

    String acquireServiceStatusBrief();

    @Deprecated
    String acquireServiceStatusAsJSON(String outputType);

    String acquireServiceStatusAsJSON();

    Map<String, String> acquireServiceStatusMap();
}
