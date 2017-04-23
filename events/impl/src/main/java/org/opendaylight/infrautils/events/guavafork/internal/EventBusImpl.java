/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.guavafork.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.inject.AbstractLifecycle;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.opendaylight.infrautils.events.EventBus} based on (a fork of) Guava.
 *
 * @author Michael Vorburger.ch
 */
@ThreadSafe
// TODO @Singleton
public class EventBusImpl extends AbstractLifecycle implements org.opendaylight.infrautils.events.EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusImpl.class);

    // max #workers - 1; copy/pasted from ForkJoinPool private
    private static final int FJP_MAX_CAP = 0x7fff;

    // similar to org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl,
    // but using true instead of false for asyncMode, seems more suitable here (TODO confirm?)
    private final ExecutorService executor = new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, LoggingThreadUncaughtExceptionHandler.toLogger(LOG), true);

    private final EventBus guavaEventBus = new EventBus(
            getClass().getName(),
            executor,
            Dispatcher.immediate(),
            (exception, context) -> LOG.error("publish({}) event dispatch to '{}'s subscribed method '{}' failed",
                    context.getEvent(), context.getSubscriber(), context.getSubscriberMethod(), exception),
            Listener.class,
            method -> method.getAnnotation(Listener.class).isThreadSafe());

    @VisibleForTesting
    volatile Object lastDeadEvent;

    @Override
    protected void start() throws Exception {
        this.subscribe(this); // for onDeadEvent();
    }

    @Override
    protected void stop() throws Exception {
        executor.shutdownNow();
    }

    @Override
    public void close() throws Exception {
        destroy();
    }

    @Listener
    protected void onDeadEvent(DeadEvent deadEvent) {
        lastDeadEvent = deadEvent.getEvent();
        // TODO have a special marker interface which prevents logging of dead events?!
        // or don't log this at all, and perhaps have an "events:dead" CLI command?!
        LOG.info("Dead event, no @Listener subscribed to: {}", lastDeadEvent);
    }

    @Override
    public CompletableFuture<Void> publish(Object event) {
        // do *NOT* requireRunning();
        CompletableFuture<Void> future;
        try {
            future = guavaEventBus.post(event);
        } catch (RejectedExecutionException e) {
            LOG.error("publish({}) failed due to RejectedExecutionException", event.toString(), e);
            future = new CompletableFuture<>();
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public Subscription subscribe(Object object) throws IllegalArgumentException, IllegalStateException {
        requireRunning();
        if (!guavaEventBus.register(object)) {
            throw new IllegalArgumentException("Class has no methods annoted with "
                    + Listener.class.getName() + ": " + object.getClass().getName());
        }
        return new SubscriptionImpl(object);
    }

    @Override
    public void unsubscribe(Subscription subscription) throws IllegalArgumentException, IllegalStateException {
        requireRunning();
        guavaEventBus.unregister(((SubscriptionImpl)subscription).objectAnnotatedWithListeners);
    }

    private static final class SubscriptionImpl implements Subscription {
        private final Object objectAnnotatedWithListeners;

        SubscriptionImpl(Object objectAnnotatedWithListeners) {
            super();
            this.objectAnnotatedWithListeners = objectAnnotatedWithListeners;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("isRunning", isRunning()).add("executor", executor).toString();
    }

}
