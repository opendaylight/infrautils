/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.sample.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * Example CLI action calling a cached service.
 *
 * @author Michael Vorburger.ch
 */
// TODO Karaf 4 @org.apache.karaf.shell.api.action.lifecycle.Service
@Command(scope = "caches-example", name = "hello", description = "Says hello")
public class SampleCacheAction extends OsgiCommandSupport {
    // TODO Karaf 4: implements Action, instead of  extends OsgiCommandSupport

    @Override
    // TODO Karaf 4: public Object execute(CommandSession session) throws Exception {
    protected Object doExecute() throws Exception {
        session.getConsole().println("hello, world!");
        return null;
    }

}
