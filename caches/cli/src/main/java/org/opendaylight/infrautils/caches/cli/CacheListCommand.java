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
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.CachePolicy;
import org.opendaylight.infrautils.caches.CacheStats;

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
        Iterable<CacheManager> allCacheManagers = cacheManagers.getAllCacheManagers();
        if (!allCacheManagers.iterator().hasNext()) {
            session.getConsole().println("No caches have been created.");
            return null;
        }
        for (CacheManager cacheManager : allCacheManagers) {
            BaseCacheConfig config = cacheManager.getConfig();
            session.getConsole().println("Cache ID: " + config.id());
            session.getConsole().println("  description: " + config.description());
            session.getConsole().println("  anchored in: " + config.anchor());

            CachePolicy policy = cacheManager.getPolicy();
            session.getConsole().println("  Policies");
            session.getConsole().println("    * statsEnabled = " + policy.statsEnabled());
            session.getConsole().println("    * maxEntries   = " + policy.maxEntries());

            CacheStats stats = cacheManager.getStats();
            session.getConsole().println("  Stats");
            session.getConsole().println("    * entries: " + stats.estimatedCurrentEntries());
            session.getConsole().println("    * hitCount: " + stats.hitCount());
            session.getConsole().println("    * missCount: " + stats.missCount());
            // session.getConsole().println("    -extensions-");
            for (Entry<String, Number> extension : stats.extensions().entrySet()) {
                session.getConsole().println("    * " + extension.getKey() + ": " + extension.getValue());
            }

            session.getConsole().println();
        }
        return null;
    }

}
