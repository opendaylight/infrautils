/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics.internal;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration properties for the metrics implementation. Karaf's OSGi ConfigAdmin service, sets this from the
 * etc/org.opendaylight.infrautils.metrics.cfg configuration file.
 *
 * @author Michael Vorburger.ch
 */
@ObjectClassDefinition
public @interface Configuration {
    // Apply any change to these defaults also to org.opendaylight.infrautils.metrics.cfg
    // (Just for clarity; they are commented out there, so these are the real defaults.)
    @AttributeDefinition(name = "threadsWatcherIntervalMS")
    int threadsWatcherIntervalMS() default 0;

    @AttributeDefinition(name = "maxThreads")
    int maxThreads() default 1000;

    @AttributeDefinition(name = "fileReporterIntervalSecs")
    int fileReporterIntervalSecs() default 0;

    @AttributeDefinition(name = "maxThreadsMaxLogIntervalSecs")
    int maxThreadsMaxLogIntervalSecs() default 60;

    @AttributeDefinition(name = "deadlockedThreadsMaxLogIntervalSecs")
    int deadlockedThreadsMaxLogIntervalSecs() default 60;
}
