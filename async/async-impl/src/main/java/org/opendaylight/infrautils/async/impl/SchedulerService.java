package org.opendaylight.infrautils.async.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opendaylight.infrautils.async.api.IAsyncConfig;
import org.opendaylight.infrautils.async.api.ISchedulerService;
import org.opendaylight.infrautils.async.api.IWorker;
import org.opendaylight.infrautils.counters.api.OccurenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerService implements ISchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private static int DEFAULT_POOL_SIZE = 4;
    private static int DEFAULT_KEEPALIVE_TIME_SEC = 0; // No timeout
    private static final String POOL_SIZE_PROP = "pool.size";
    private static final String KEEPALIVE_TIME_PROP = "keepAliveTimeout";

    private final Map<String, ScheduledThreadPoolExecutor> poolNameToExecutor;
    private final Map<String, PoolData> identifierToPoolData;
    private final Map<String, ScheduledFuture<?>> identifierToTaskReference;
    private IAsyncConfig config;

    public SchedulerService(IAsyncConfig config) {
        poolNameToExecutor = new ConcurrentHashMap<>();
        identifierToPoolData = new ConcurrentHashMap<>();
        identifierToTaskReference = new ConcurrentHashMap<>();
        this.config = config;
    }

    @Override
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    class PoolData {
        public String poolName;
        public RunnableWrapperForWorker runnable;

        public PoolData(String poolName, IWorker worker) {
            this.poolName = poolName;
            runnable = new RunnableWrapperForWorker(worker);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PoolData other = (PoolData) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (poolName == null) {
                if (other.poolName != null)
                    return false;
            } else if (!poolName.equals(other.poolName))
                return false;
            return true;
        }

        private SchedulerService getOuterType() {
            return SchedulerService.this;
        }
    }

    @Override
    public void defineTriggerableWorker(String identifier, String poolName, IWorker worker) {
        if (worker == null) {
            throw new RuntimeException("Worker is null.");
        }
        if (identifierToPoolData.containsKey(identifier)) {
            throw new RuntimeException("Worker Group with the id: " + identifier + " already exists!");
        }

        logger.debug("define Triggrable Worker: (identifier=\"{}\", poolName=\"{}\")", identifier, poolName);

        identifierToPoolData.put(identifier, new PoolData(poolName, worker));
    }

    public static class RunnableWrapperForWorker implements Runnable {

        private IWorker worker;

        public RunnableWrapperForWorker(IWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                logger.trace("Worker calling work!");
                worker.work();
            } catch (Throwable t) {
                SchedulerServiceCounters.exception_thrown_in_worker.inc();
                logger.warn("", t);
            } finally {
                if (Thread.interrupted()) {
                    SchedulerServiceCounters.thread_interrupted_status_cleared.inc();
                    logger.debug("Clear thread {} class {} interrupted status", Thread.currentThread().getName(),
                            worker.getClass().getSimpleName());
                }
            }
        }

        public IWorker getWorker() {
            return worker;
        }
    }

    @Override
    public void triggerExecution(String identifier) {
        logger.debug("triggerWorkerGroup (identifier={}", identifier);
        PoolData poolData = identifierToPoolData.get(identifier);
        if (poolData != null) {
            ScheduledThreadPoolExecutor executor = getExecutorByPool(poolData.poolName);
            executor.execute(poolData.runnable);
        } else {
            logger.error("Worker Group with id: " + identifier + " was triggered to work, but doesn't exist");
        }

    }

    @Override
    public void scheduleWorkerAtFixedRate(String identifier, String poolName, IWorker worker, long delay, long rate, TimeUnit t) {
        if (identifierToPoolData.containsKey(identifier)) {
            throw new RuntimeException("Worker Group with the id: " + identifier + " already exists!");
        }

        ScheduledThreadPoolExecutor executor = getExecutorByPool(poolName);

        PoolData poolData = new PoolData(poolName, worker);
        identifierToPoolData.put(identifier, poolData);

        ScheduledFuture<?> taskReference = executor.scheduleAtFixedRate(poolData.runnable, delay, rate, t);
        identifierToTaskReference.put(identifier, taskReference);
    }

    @Override
    public boolean cancel(String identifier) {
        PoolData poolData = identifierToPoolData.get(identifier);
        if (poolData == null) {
            logger.warn("Worker Group with id: " + identifier + " was cancelled, but it doesn't exist.");
            return false;
        }

        ScheduledThreadPoolExecutor executor = poolNameToExecutor.get(poolData.poolName);
        if (executor == null) {
            logger.warn("Worker Group with id: " + identifier + " was cancelled, but it has no executor.");
            return false;
        }

        ScheduledFuture<?> scheduledFuture = identifierToTaskReference.remove(identifier);
        if (scheduledFuture == null) {
            logger.warn("Worker Group with id: " + identifier + " was cancelled, but it has no scheduled future.");
            return false;
        }

        boolean wasRemoved = scheduledFuture.cancel(true);

        identifierToPoolData.remove(identifier);

        if (!identifierToPoolData.containsValue(poolData)) {
            poolNameToExecutor.remove(poolData.poolName);
        }

        return wasRemoved;
    }

    private ScheduledThreadPoolExecutor getExecutorByPool(String poolName) {
        ScheduledThreadPoolExecutor executor = poolNameToExecutor.get(poolName);
        if (executor == null) {
            synchronized (this) {
                executor = poolNameToExecutor.get(poolName);
                if (executor == null) {
                    int keepAliveTime = getKeepaliveTime(poolName);
                    int poolSize = getPoolSize(poolName);
                    if (keepAliveTime > 0) {
                        executor = ThreadPoolExecutorFactory.create(poolName, poolSize, keepAliveTime,
                                TimeUnit.SECONDS);
                    } else {
                        executor = ThreadPoolExecutorFactory.create(poolName, poolSize);
                    }
                    logger.debug("Created thread pool: " + poolName + " with amount of threads: " + poolSize);
                    poolNameToExecutor.put(poolName, executor);
                }
            }
        }

        return executor;
    }

    @Override
    public ScheduledFuture<?> scheduleWorkerOnce(String poolName, IWorker worker, long delay, TimeUnit timeUnit) {
        ScheduledThreadPoolExecutor executor = getExecutorByPool(poolName);
        return executor.schedule(new RunnableWrapperForWorker(worker), delay, timeUnit);
    }

    @Override
    public void scheduleWorkerAtFixedRate(String identifier, String poolName, IWorker worker, long rate, TimeUnit t) {
        scheduleWorkerAtFixedRate(identifier, poolName, worker, 0, rate, t);
    }

    @Override
    public void scheduleWorkerAtFixedRate(String identifier, IWorker worker, long delay, long rate, TimeUnit t) {
        scheduleWorkerAtFixedRate(identifier, DEFAULT_POOL, worker, delay, rate, t);

    }

    @Override
    public void scheduleWorkerAtFixedRate(String identifier, IWorker worker, long rate, TimeUnit t) {
        scheduleWorkerAtFixedRate(identifier, DEFAULT_POOL, worker, 0, rate, t);
    }

    @Override
    public void scheduleWorkersOnce(String poolName, long delay, TimeUnit t, IWorker... workers) {
        for (IWorker worker : workers) {
            scheduleWorkerOnce(poolName, worker, delay, t);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleWorkerOnce(IWorker worker, long delay, TimeUnit t) {
        return scheduleWorkerOnce(DEFAULT_POOL, worker, delay, t);
    }

    private int getPoolSize(String poolName) {
        return config.getInt("/pools/" + poolName + "/" + POOL_SIZE_PROP, DEFAULT_POOL_SIZE);
    }

    private int getKeepaliveTime(String poolName) {
        return config.getInt("/pools/" + poolName + "/" + KEEPALIVE_TIME_PROP, DEFAULT_KEEPALIVE_TIME_SEC);
    }

    enum SchedulerServiceCounters {
        exception_thrown_in_worker, //
        thread_interrupted_status_cleared, //
        ;

        private OccurenceCounter counter;

        SchedulerServiceCounters() {
            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), name());
        }

        public void inc() {
            counter.inc();
        }
    }
}
