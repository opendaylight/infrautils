/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;

/**
 * CLI "cache:clear" command.
 *
 * @author Michael Vorburger.ch
 */
// TODO Karaf 4 @org.apache.karaf.shell.api.action.lifecycle.Service
@Command(scope = "cache", name = "clear", description = "Clear (evict) all entries of all caches")
public class CacheClearCommand extends OsgiCommandSupport {
    // TODO Karaf 4: implements Action, instead of  extends OsgiCommandSupport

    // TODO introduce an argument (with auto-completion!) allowing also to clear just 1 named cache

    private final CacheManagers cacheManagers;

    public CacheClearCommand(CacheManagers cacheManagers) {
        this.cacheManagers = cacheManagers;
    }

    @Override
    // TODO Karaf 4: public Object execute(CommandSession session) throws Exception {
    protected Object doExecute() throws Exception {
        for (CacheManager cacheManager : cacheManagers.getAllCacheManagers()) {
            cacheManager.evictAll();
        }
        session.getConsole().println("Succesfully cleared all caches.");
        return null;
    }

}
