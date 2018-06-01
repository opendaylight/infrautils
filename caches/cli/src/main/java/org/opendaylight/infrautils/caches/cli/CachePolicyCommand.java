/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nullable;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.CachePolicyBuilder;

/**
 * CLI "cache:policy cacheID policyKey policyValue" command.
 *
 * @author Michael Vorburger.ch
 */
@Command(scope = "cache", name = "policy", description = "Change a cache's policy")
@Service
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't get that Karaf will set fields
public class CachePolicyCommand implements Action {
    // TODO use ANSI sequences for bold/color.. see e.g. how (Karaf 4's) "info" command does it

    @Argument(index = 0, name = "cache ID", description = "ID of the cache, as shown by cache:list", required = true)
    String cacheID;

    @Argument(index = 1, name = "policy key", description = "Key of cache policy to change", required = true)
    String policyKey;

    @Argument(index = 2, name = "policy value", description = "Value of cache policy to change", required = true)
    String policyValue;

    @Reference
    private CacheManagers cacheManagers;

    @Override
    @Nullable
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() {
        CacheManager cacheManager = cacheManagers.getCacheManager(cacheID);
        CachePolicyBuilder cachePolicyBuilder = new CachePolicyBuilder().from(cacheManager.getPolicy());
        switch (policyKey) {
            case "maxEntries":
                cachePolicyBuilder.maxEntries(Long.parseLong(policyValue));
                break;
            case "statsEnabled":
                cachePolicyBuilder.statsEnabled(Boolean.parseBoolean(policyValue));
                break;
            default:
                throw new UnsupportedOperationException(
                        "TODO: Implementation specific cache policies to be implemented..");
        }
        cacheManager.setPolicy(cachePolicyBuilder.build());
        System.out.println("Succesfully updated cache policy");
        return null;
    }

}
