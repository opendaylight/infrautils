/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.internal;

public interface DiagStatusServiceImplMBean {

    String acquireServiceStatus();

    String acquireServiceStatusDetailed();

    String acquireServiceStatusBrief();

    String acquireServiceStatusJSON(String outputType);

    java.util.HashMap acquireServiceStatusMAP();
}