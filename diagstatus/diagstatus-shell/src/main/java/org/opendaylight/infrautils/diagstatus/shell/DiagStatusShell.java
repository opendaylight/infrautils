/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * CLI for showing service status {@link DiagStatusShell}.
 *
 * @author Faseela K
 */

@Command(scope = "infrautils", name = "showstatus", description = "getAllServicesStatus")
public class DiagStatusShell extends OsgiCommandSupport  {

    @Override
    protected Object doExecute() throws Exception {
        // TODO
        return null;
    }

}


