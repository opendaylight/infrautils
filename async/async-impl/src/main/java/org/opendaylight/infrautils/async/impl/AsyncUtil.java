package org.opendaylight.infrautils.async.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import org.opendaylight.infrautils.async.impl.AsyncInvocationProxy.AsyncCounters;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncUtil {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncUtil.class);
    private static BundleContext bundleContext = null;

    public static void setBundleContext(BundleContext context) {
        AsyncUtil.bundleContext = context;
    }

    public static Object makeAsync(Object instance, Class<?>... ifaces)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> loadClass = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            try {
                loadClass = bundle.loadClass(ifaces[0].getName());
            } catch (ClassNotFoundException e) {

            }
        }
        if (loadClass == null) {
            logger.error("Searched all bundles but class wasn't found: " + ifaces[0].getName());
        }
        AsyncInvocationProxy invocationHandler = new AsyncInvocationProxy(instance);
        if (invocationHandler.getWorkMode() == AsyncInvocationProxy.WORK_MODE_IMMEDIATE) {
            logger.info("Create async proxy request in code was ignored due to configuration for: " + instance.getClass().getName());
            return instance;
        } else {
            AsyncCounters.proxy_created.inc();
            logger.debug("Created async proxy for: " + instance.getClass().getName() + " with queue size: "
                    + invocationHandler.getQueueSize());
            return Proxy.newProxyInstance(loadClass.getClassLoader(), ifaces, invocationHandler);
        }
    }
}
