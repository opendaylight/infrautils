package org.opendaylight.infrautils.async.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorFactory {

    private static Callable<ScheduledThreadPoolExecutor> callableForUnitTests = null;

    public static void setMockCallable(Callable<ScheduledThreadPoolExecutor> callable) {
        ThreadPoolExecutorFactory.callableForUnitTests = callable;
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private String name;
        private int counter = 0;

        public NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(name + "_" + counter++);
            return thread;
        }
    }

    public static ScheduledThreadPoolExecutor create(String poolName, int corePoolSize) {
        if (callableForUnitTests == null) {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize,
                    new NamedThreadFactory(poolName));
            return executor;
        } else {
            try {
                return callableForUnitTests.call();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static ScheduledThreadPoolExecutor create(String poolName, int corePoolSize, long keepAliveTime,
            TimeUnit keepAliveUnit) {
        ScheduledThreadPoolExecutor executor = create(poolName, corePoolSize);
        executor.allowCoreThreadTimeOut(true);
        executor.setKeepAliveTime(keepAliveTime, keepAliveUnit);
        return executor;
    }
}
