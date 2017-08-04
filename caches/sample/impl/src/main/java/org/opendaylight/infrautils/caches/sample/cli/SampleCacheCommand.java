/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.caches.sample.cli;

import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.TimeUnit;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.infrautils.caches.sample.SampleService;

/**
 * Example CLI action calling a cached service.
 *
 * @author Michael Vorburger.ch
 */
// TODO Karaf 4 @org.apache.karaf.shell.api.action.lifecycle.Service
@Command(scope = "cache-example", name = "hello", description = "Says hello")
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't get that Karaf will set field
public class SampleCacheCommand extends OsgiCommandSupport {
    // TODO Karaf 4: implements Action, instead of  extends OsgiCommandSupport

    @Argument(name = "who", description = "who to greet", required = true, valueToShowInHelp = "world")
    String whoToGreet;

    private final SampleService sampleService;

    public SampleCacheCommand(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @Override
    // TODO Karaf 4: public Object execute(CommandSession session) throws Exception {
    protected Object doExecute() throws Exception {
        Stopwatch stopWatch = Stopwatch.createStarted();
        String hello = sampleService.sayHello(whoToGreet);
        long ms = stopWatch.elapsed(TimeUnit.MILLISECONDS);

        session.getConsole().println(ms + "ms: " + hello);
        return null;
    }

}
