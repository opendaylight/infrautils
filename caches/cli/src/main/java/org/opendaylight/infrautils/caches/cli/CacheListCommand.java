/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import java.util.Map.Entry;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.caches.BaseCacheConfig;
import org.opendaylight.infrautils.caches.ops.CacheManager;
import org.opendaylight.infrautils.caches.ops.CacheManagers;
import org.opendaylight.infrautils.caches.ops.CachePolicy;
import org.opendaylight.infrautils.caches.ops.CacheStats;

/**
 * CLI "cache:list" command.
 *
 * @author Michael Vorburger.ch
 */
// TODO Karaf 4 @org.apache.karaf.shell.api.action.lifecycle.Service
@Command(scope = "cache", name = "list", description = "Lists all caches")
public class CacheListCommand extends OsgiCommandSupport {
    // TODO Karaf 4: implements Action, instead of  extends OsgiCommandSupport

    // TODO use ANSI sequences for bold/color.. see e.g. how (Karaf 4's) "info" command does it

    private final CacheManagers cacheManagers;

    public CacheListCommand(CacheManagers cacheManagers) {
        this.cacheManagers = cacheManagers;
    }

    @Override
    // TODO Karaf 4: public Object execute(CommandSession session) throws Exception {
    protected Object doExecute() throws Exception {
        for (CacheManager cacheManager : cacheManagers.getAllCacheManagers()) {
            BaseCacheConfig config = cacheManager.getConfig();
            session.getConsole().print("Cache ID:    " + config.id());
            session.getConsole().print("  Description: " + config.description());
            session.getConsole().print("  anchored in: " + config.anchor());

            CachePolicy policy = cacheManager.getPolicy();
            session.getConsole().print("  Policies");
            session.getConsole().print("    * statsEnabled = " + policy.statsEnabled());
            session.getConsole().print("    * maxEntries   = " + policy.maxEntries());

            CacheStats stats = cacheManager.getStats();
            session.getConsole().print("  Stats");
            session.getConsole().print("    * entries:   " + stats.estimatedCurrentEntries());
            session.getConsole().print("    * hitCount:  " + stats.hitCount());
            session.getConsole().print("    * missCount: " + stats.missCount());
            session.getConsole().print("    =extensions=");
            for (Entry<String, Number> extension : stats.extensions().entrySet()) {
                session.getConsole().print("    * " + extension.getKey() + ": " + extension.getValue());
            }

            session.getConsole().println();
        }
        return null;
    }

}
