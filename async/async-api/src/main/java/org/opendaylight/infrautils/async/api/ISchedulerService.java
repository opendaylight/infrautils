package org.opendaylight.infrautils.async.api;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface ISchedulerService {
    public static final String DEFAULT_POOL = "DEFAULT_POOL";

    void sleep(int millis);

    void defineTriggerableWorker(String identifier, String poolName, IWorker worker);

    void triggerExecution(String identifier);

    boolean cancel(String identifier);

    void scheduleWorkerAtFixedRate(String identifier, String poolName, IWorker worker, long rate, TimeUnit t);

    void scheduleWorkerAtFixedRate(String identifier, String poolName, IWorker worker, long delay, long rate, TimeUnit t);

    void scheduleWorkerAtFixedRate(String identifier, IWorker worker, long rate, TimeUnit t);

    void scheduleWorkerAtFixedRate(String identifier, IWorker worker, long delay, long rate, TimeUnit t);

    ScheduledFuture<?> scheduleWorkerOnce(String poolName, IWorker worker, long delay, TimeUnit t);

    ScheduledFuture<?> scheduleWorkerOnce(IWorker worker, long delay, TimeUnit t);

    void scheduleWorkersOnce(String poolName, long delay, TimeUnit t, IWorker... workers);

}
