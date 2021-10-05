/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;

/**
 * CLI "cache:clear" command.
 *
 * @author Michael Vorburger.ch
 * @deprecated This interface will be retired as part of https://jira.opendaylight.org/browse/INFRAUTILS-82
 */
@Deprecated(since = "2.0.7", forRemoval = true)
@Command(scope = "cache", name = "clear", description = "Clear (evict) all entries of all caches")
@Service
public class CacheClearCommand implements Action {
    // TODO introduce an argument (with auto-completion!) allowing also to clear just 1 named cache

    @Reference
    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    private CacheManagers cacheManagers;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public @Nullable Object execute() {
        for (CacheManager cacheManager : cacheManagers.getAllCacheManagers()) {
            cacheManager.evictAll();
        }
        System.out.println("Successfully cleared all caches.");
        return null;
    }
}
