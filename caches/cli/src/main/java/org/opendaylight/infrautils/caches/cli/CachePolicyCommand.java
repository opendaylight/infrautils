/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.caches.CacheManager;
import org.opendaylight.infrautils.caches.CacheManagers;
import org.opendaylight.infrautils.caches.CachePolicyBuilder;

/**
 * CLI "cache:policy cacheID policyKey policyValue" command.
 *
 * @author Michael Vorburger.ch
 */
// TODO Karaf 4 @org.apache.karaf.shell.api.action.lifecycle.Service
@Command(scope = "cache", name = "policy", description = "Change a cache's policy")
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't get that Karaf will set fields
public class CachePolicyCommand extends OsgiCommandSupport {
    // TODO Karaf 4: implements Action, instead of  extends OsgiCommandSupport

    // TODO use ANSI sequences for bold/color.. see e.g. how (Karaf 4's) "info" command does it

    @Argument(index = 0, name = "cache ID", description = "ID of the cache, as shown by cache:list", required = true)
    String cacheID;

    @Argument(index = 1, name = "policy key", description = "Key of cache policy to change", required = true)
    String policyKey;

    @Argument(index = 2, name = "policy value", description = "Value of cache policy to change", required = true)
    String policyValue;

    private final CacheManagers cacheManagers;

    public CachePolicyCommand(CacheManagers cacheManagers) {
        this.cacheManagers = cacheManagers;
    }

    @Override
    // TODO Karaf 4: public Object execute(CommandSession session) throws Exception {
    protected Object doExecute() throws Exception {
        CacheManager cacheManager = cacheManagers.getCacheManager(cacheID);
        CachePolicyBuilder cachePolicyBuilder = new CachePolicyBuilder().from(cacheManager.getPolicy());
        if ("maxEntries".equals(policyKey)) {
            cachePolicyBuilder.maxEntries(Long.parseLong(policyValue));
        } else if ("statsEnabled".equals(policyKey)) {
            cachePolicyBuilder.statsEnabled(Boolean.parseBoolean(policyValue));
        } else {
            throw new UnsupportedOperationException("TODO: Implementation specific cache policies to be implemented..");
        }
        cacheManager.setPolicy(cachePolicyBuilder.build());
        session.getConsole().println("Succesfully updated cache policy");
        return null;
    }

}
