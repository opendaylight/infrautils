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
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.infrautils.caches.sample.SampleService;

/**
 * Example CLI action calling a cached service.
 *
 * @author Michael Vorburger.ch
 */
@Command(scope = "cache-example", name = "hello", description = "Says hello")
@Service
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't get that Karaf will set field
public class SampleCacheCommand implements Action {

    @Argument(name = "who", description = "who to greet", required = true, valueToShowInHelp = "world")
    String whoToGreet;

    @Reference
    private SampleService sampleService;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public @Nullable Object execute() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        String hello = sampleService.sayHello(whoToGreet);
        long ms = stopWatch.elapsed(TimeUnit.MILLISECONDS);

        System.out.println(ms + "ms: " + hello);
        return null;
    }
}
