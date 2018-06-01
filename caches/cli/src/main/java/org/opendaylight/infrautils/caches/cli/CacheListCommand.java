/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
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
@Command(scope = "cache", name = "list", description = "Lists all caches")
@Service
public class CacheListCommand implements Action {
    // TODO use ANSI sequences for bold/color.. see e.g. how (Karaf 4's) "info" command does it

    @Reference
    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    private CacheManagers cacheManagers;

    @Override
    @Nullable
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() {
        Iterable<CacheManager> allCacheManagers = cacheManagers.getAllCacheManagers();
        if (!allCacheManagers.iterator().hasNext()) {
            System.out.println("No caches have been created.");
            return null;
        }
        for (CacheManager cacheManager : allCacheManagers) {
            BaseCacheConfig config = cacheManager.getConfig();
            System.out.println("Cache ID: " + config.id());
            System.out.println("  description: " + config.description());
            System.out.println("  anchored in: " + config.anchor());

            CachePolicy policy = cacheManager.getPolicy();
            System.out.println("  Policies");
            System.out.println("    * statsEnabled = " + policy.statsEnabled());
            System.out.println("    * maxEntries   = " + policy.maxEntries());

            CacheStats stats = cacheManager.getStats();
            System.out.println("  Stats");
            System.out.println("    * entries: " + stats.estimatedCurrentEntries());
            System.out.println("    * hitCount: " + stats.hitCount());
            System.out.println("    * missCount: " + stats.missCount());
            // System.out.println("    -extensions-");
            for (Entry<String, Number> extension : stats.extensions().entrySet()) {
                System.out.println("    * " + extension.getKey() + ": " + extension.getValue());
            }

            System.out.println();
        }
        return null;
    }

}
