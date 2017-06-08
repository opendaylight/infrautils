/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI for showing remote service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "list", description = "show the status of registered services")
public class DiagStatusCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    @Option(name = "-n", aliases = {"--node"}, required = false, multiValued = false)
    String nip;
    @Option(name = "-a", aliases = {"--all"}, required = false, multiValued = false)
    String all;
    @Option(name = "-h", aliases = {"--help"}, required = false, multiValued = false)
    String help;

    @Override
    protected Object doExecute() throws Exception {
        //TODO will come in subsequent patches
        return null;
    }
}
