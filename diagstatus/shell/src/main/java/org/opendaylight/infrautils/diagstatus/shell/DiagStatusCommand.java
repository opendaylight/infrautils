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
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * CLI for showing remote service status.
 *
 * @author Faseela K
 */
@Command(scope = "diagstatus", name = "list", description = "show the status of registered services")
public class DiagStatusCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusCommand.class);

    private final DiagStatusService diagStatusService;

    @Option(name = "-n", aliases = {"--node"}, required = false, multiValued = false)
    String nip;
    @Option(name = "-a", aliases = {"--all"}, required = false, multiValued = false)
    String all;

    @Inject
    public DiagStatusCommand(DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
    }

    @Override
    protected Object doExecute() throws Exception {
        // TODO this is just for basic testing. More detailed implementation will come in subsequent patches
        session.getConsole().print(diagStatusService.getAllServiceDescriptors());
        return null;
    }
}
