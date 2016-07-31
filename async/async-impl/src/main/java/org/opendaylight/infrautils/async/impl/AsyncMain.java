package org.opendaylight.infrautils.async.impl;

import org.opendaylight.infrautils.async.api.IAsyncConfig;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncMain {
    protected static final Logger logger = LoggerFactory.getLogger(AsyncMain.class);

    private SchedulerService schedulerService;
    private IAsyncConfig config;

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
