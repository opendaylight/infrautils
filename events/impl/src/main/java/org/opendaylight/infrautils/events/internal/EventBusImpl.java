/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.internal;

import com.google.common.reflect.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.PreDestroy;
import org.opendaylight.infrautils.events.EventBus;
import org.opendaylight.infrautils.events.EventListener;
import org.opendaylight.infrautils.events.guavafork.internal.AsyncEventBus;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EventBus}.
 *
 * @author Michael Vorburger.ch
 */
// TODO @Singleton
public class EventBusImpl implements EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusImpl.class);

    // max #workers - 1; copy/pasted from ForkJoinPool private
    private static final int FJP_MAX_CAP = 0x7fff;

    // similar to org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl,
    // but using true instead of false for asyncMode, seems more suitable here (TODO confirm?)
    private final ExecutorService executor = new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, LoggingThreadUncaughtExceptionHandler.toLogger(LOG), true);

    private final AsyncEventBus guavaEventBus = new AsyncEventBus(
            executor, (exception, context) -> LOG.error("Could not dispatch event {} to subscribe {} method {}",
                    context.getEvent(), context.getSubscriber(), context.getSubscriberMethod(), exception));

    @Override
    @PreDestroy
    public void close() throws Exception {
        executor.shutdownNow();
    }

    @Override
    public CompletableFuture<Void> publish(Object event) {
        guavaEventBus.post(event);
        // TODO actually implement this instead of faking it..
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <E> Subscription subscribe(Class<E> eventClass, EventListener<? super E> listener) {
        return null;
    }

    @Override
    public <E> Subscription subscribe(TypeToken<E> reifiedEventClass, EventListener<E> listener) {
        return null;
    }

    @Override
    public Subscription subscribe(Object object) throws IllegalArgumentException {
        guavaEventBus.register(object);
        return new SubscriptionImpl(object);
    }

    @Override
    public void unsubscribe(Subscription subscription) throws IllegalArgumentException {
        guavaEventBus.unregister(((SubscriptionImpl)subscription).objectAnnotatedWithListeners);
    }

    private static final class SubscriptionImpl implements Subscription {
        private final Object objectAnnotatedWithListeners;

        SubscriptionImpl(Object objectAnnotatedWithListeners) {
            super();
            this.objectAnnotatedWithListeners = objectAnnotatedWithListeners;
        }

    }

}
