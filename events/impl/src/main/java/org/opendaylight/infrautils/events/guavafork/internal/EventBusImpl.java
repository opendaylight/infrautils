/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.guavafork.internal;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.PreDestroy;
import org.opendaylight.infrautils.events.EventListener;
import org.opendaylight.infrautils.events.Listener;
import org.opendaylight.infrautils.utils.concurrent.LoggingThreadUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.opendaylight.infrautils.events.EventBus} based on (a fork of) Guava.
 *
 * @author Michael Vorburger.ch
 */
// TODO @Singleton
public class EventBusImpl implements org.opendaylight.infrautils.events.EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusImpl.class);

    // max #workers - 1; copy/pasted from ForkJoinPool private
    private static final int FJP_MAX_CAP = 0x7fff;

    // similar to org.opendaylight.infrautils.jobcoordinator.internal.JobCoordinatorImpl,
    // but using true instead of false for asyncMode, seems more suitable here (TODO confirm?)
    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(new ForkJoinPool(
            Math.min(FJP_MAX_CAP, Runtime.getRuntime().availableProcessors()),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory, LoggingThreadUncaughtExceptionHandler.toLogger(LOG), true));

    private final EventBus guavaEventBus = new EventBus(
            getClass().getName(),
            executor,
            Dispatcher.immediate(),
            (exception, context) -> LOG.error("Could not dispatch event {} to subscribe {} method {}",
                    context.getEvent(), context.getSubscriber(), context.getSubscriberMethod(), exception));

    @Override
    @PreDestroy
    public void close() throws Exception {
        executor.shutdownNow();
    }

    @Override
    public CompletableFuture<Void> publish(Object event) {
        return guavaEventBus.post(event);
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
        if (!guavaEventBus.register(object)) {
            throw new IllegalArgumentException("Class has no methods annoted with "
                    + Listener.class.getName() + ": " + object.getClass().getName());
        }
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
