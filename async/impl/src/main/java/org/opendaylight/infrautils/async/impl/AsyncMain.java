/*
 * Copyright (c) 2016 Hewlett Packard Enterprise, Co. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.async.impl;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncMain {
    protected static final Logger logger = LoggerFactory.getLogger(AsyncMain.class);

    private SchedulerService schedulerService;
    private AsyncConfig config;

    public AsyncMain() {
        config = new AsyncConfig();
        schedulerService = new SchedulerService(config);
        AsyncInvocationProxy.setSchedulerService(schedulerService);
        AsyncInvocationProxy.setAsyncConfig(config);
    }

    public void setContext(BundleContext bcontext) {
        AsyncInvocationProxy.setBundleContext(bcontext);
    }

    public void initialize() {
        try {
            System.out.println("Async init called");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clean() {
    }
}
