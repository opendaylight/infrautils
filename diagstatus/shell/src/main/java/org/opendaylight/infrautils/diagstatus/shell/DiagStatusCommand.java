/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import com.google.common.annotations.VisibleForTesting;
import java.net.InetAddress;
import java.util.Date;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.diagstatus.DiagStatusServiceMBean;

/**
 * CLI for showing registered service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "showSvcStatus", description = "show the status of registered services")
@Service
public class DiagStatusCommand implements Action {
    @Reference
    @VisibleForTesting
    DiagStatusServiceMBean diagStatusServiceMBean;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public @Nullable Object execute() throws Exception {
        System.out.println("Timestamp: " + new Date());
        System.out.println(getLocalStatusSummary(InetAddress.getLoopbackAddress()));
        return null;
    }

    @VisibleForTesting
    String getLocalStatusSummary(InetAddress memberAddress) {
        return "Node IP Address: " + memberAddress.getHostAddress() + "\n"
                + diagStatusServiceMBean.acquireServiceStatusDetailed();
    }
}
