package org.opendaylight.infrautils.async.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.infrautils.counters.impl.OccurenceCounter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncInvocationProxy extends AbstractInvocationHandler {
    private static final String DEFAULT_POOL = "DEFAULT_POOL";
    public static final String CONFIG_WORK_MODE = "work.mode";
    public static final String CONFIG_QUEUE_SIZE = "queue.size";
    public static final int WORK_MODE_IMMEDIATE = 0;
    public static final int WORK_MODE_INDIRECT = 1;

    protected static final Logger logger = LoggerFactory.getLogger(AsyncInvocationProxy.class);

    private Class<?> classType;
    private volatile LinkedBlockingQueue<MethodCallMessage> queue;
    private Map<Method, Boolean> methodAnnotationMap = new ConcurrentHashMap<>();
    private volatile int workMode;
    private Object theInstance;
    private volatile AnnotationMode annotationMode = AnnotationMode.NORMAL;

    private static IAsyncConfig config;
    private static ISchedulerService schedulerService;

    public final static int QUEUE_DEFAULT_SIZE = 100000;
    private volatile int lastQueueSize = 0;
    private volatile int buildUpCounter = 0;
    private AtomicBoolean buildUpUpdating = new AtomicBoolean(false);
    private volatile long lastBuildUpTS = 0;

    public static void setSchedulerService(ISchedulerService schedulerService) {
        AsyncInvocationProxy.schedulerService = schedulerService;
    }

    public static void setAsyncConfig(IAsyncConfig config) {
        AsyncInvocationProxy.config = config;
    }

    public static void setBundleContext(BundleContext bcontext) {
        AsyncUtil.setBundleContext(bcontext);
    }

    public AsyncInvocationProxy(Object theInstance) throws InstantiationException, IllegalAccessException {
        this.theInstance = theInstance;
        this.classType = theInstance.getClass();
        int queueSize = getQueueSize();
        if (queueSize <= 0) {
            logger.error("Queue size can't be smaller than 1. Size is: " + queueSize);
            queueSize = QUEUE_DEFAULT_SIZE;
        }

        workMode = getWorkMode();
        if (workMode == WORK_MODE_IMMEDIATE) {
            return;
        }

        annotationMode = getAnnotationMode();

        String poolName = getPoolName();
        queue = new LinkedBlockingQueue<MethodCallMessage>(queueSize);

        QueueReaderWorker queueReaderWorker = new QueueReaderWorker(queue, theInstance);

        schedulerService.defineTriggerableWorker(classType.getCanonicalName(), poolName, queueReaderWorker);
    }

    private AnnotationMode getAnnotationMode() {
        validateLoadingOrder();
        String mode = config.getString("annotation.mode", "normal").toLowerCase();
        if (mode.equals("normal")) {
            return AnnotationMode.NORMAL;
        } else if (mode.equals("reversed")) {
            return AnnotationMode.REVERSED;
        }
        logger.warn("Configuration for Async annotation mode isn't recognized, fallbacking to Normal mode");
        return AnnotationMode.NORMAL;
    }

    private void validateLoadingOrder() {
        if (config == null) {
            throw new RuntimeException("Main module wasn't initialized yet.");
        }
    }

    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("invoked proxy for class: {}, method: {}, args: {}", classType.getSimpleName(), method,
                Arrays.toString(args));

        if (shouldInvokeOnThisThread(method)) {
            return method.invoke(theInstance, args);
        }

        // indirect
        MethodCallMessage msg = new MethodCallMessage(method, args);
        addMessageToQueue(msg);

        schedulerService.triggerExecution(classType.getCanonicalName());
        return null;
    }

    private void addMessageToQueue(MethodCallMessage msg) {
        if (!queue.offer(msg)) {
            AsyncCounters.capped_async_queue_full_msg_drop.inc();
        }
        validateBuildUp();
    }

    private void validateBuildUp() {
        if (System.currentTimeMillis() - lastBuildUpTS > 750) { // 0.75 secs
            if (buildUpUpdating.compareAndSet(false, true)) { // only one
                                                              // updater at a
                                                              // time
                lastBuildUpTS = System.currentTimeMillis();
                int curQueueSize = queue.size();
                if (curQueueSize - lastQueueSize > 0) { // build up
                    ++buildUpCounter;
                    if (buildUpCounter >= 5) { // 3.75 secs
                        AsyncCounters.queue_build_up.inc();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Queue build up in type: " + classType.getSimpleName() + " current size is: "
                                    + curQueueSize);
                        }
                    }
                } else {
                    buildUpCounter = 0;
                }
                lastQueueSize = curQueueSize;
                buildUpUpdating.set(false);
            }
        }
    }

    private boolean shouldInvokeOnThisThread(Method method) throws NoSuchMethodException {
        if (annotationMode == AnnotationMode.NORMAL) {
            return !isAsyncMethod(method);
        } else {
            return isSyncMethod(method);
        }
    }

    private boolean checkAnnotationAndCache(Method method, Class<? extends Annotation> annotation)
            throws NoSuchMethodException {
        if (!methodAnnotationMap.containsKey(method)) {
            boolean syncAnnotationPresent = isAnnotationPresent(classType, method, annotation);
            methodAnnotationMap.put(method, syncAnnotationPresent);
        }
        return methodAnnotationMap.get(method);
    }

    private boolean isSyncMethod(Method method) throws NoSuchMethodException {
        return checkAnnotationAndCache(method, SyncMethod.class);
    }

    private boolean isAsyncMethod(Method method) throws NoSuchMethodException {
        return checkAnnotationAndCache(method, AsyncMethod.class);
    }

    private boolean isAnnotationPresent(Class<?> clazz, Method expectedMethod, Class<? extends Annotation> annotation)
            throws NoSuchMethodException, SecurityException {
        Method method = getMethod(clazz, expectedMethod);
        if (method != null && method.isAnnotationPresent(annotation)) {
            return true;
        }

        for (Class<?> interfaze : clazz.getInterfaces()) {
            if (isAnnotationPresent(interfaze, expectedMethod, annotation)) {
                return true;
            }
        }

        return false;
    }

    private Method getMethod(Class<?> clazz, Method expectedMethod) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(expectedMethod.getName())
                    && Arrays.equals(method.getParameterTypes(), expectedMethod.getParameterTypes())) {
                return method;
            }
        }

        return null;
    }

    public Integer getWorkMode() {
        validateLoadingOrder();
        return config.getInt(IAsyncConfig.CONFIG_PROXIES + classType.getCanonicalName() + "/" + CONFIG_WORK_MODE,
                WORK_MODE_INDIRECT);
    }

    public Integer getQueueSize() {
        validateLoadingOrder();
        return config.getInt(IAsyncConfig.CONFIG_PROXIES + classType.getCanonicalName() + "/" + CONFIG_QUEUE_SIZE,
                QUEUE_DEFAULT_SIZE);
    }

    private String getPoolName() {
        validateLoadingOrder();
        return config.getString(
                IAsyncConfig.CONFIG_PROXIES + classType.getCanonicalName() + "/" + IAsyncConfig.CONFIG_POOL_NAME,
                DEFAULT_POOL);
    }

    enum AsyncCounters {
        queue_build_up, //
        capped_async_queue_full_msg_drop, //
        proxy_created //
        ;

        private OccurenceCounter counter;

        private AsyncCounters() {
            counter = new OccurenceCounter(getClass().getEnclosingClass().getSimpleName(), name(), name());
        }

        public void inc() {
            counter.inc();
        }
    }

    private enum AnnotationMode {
        NORMAL, REVERSED;
    }
}
