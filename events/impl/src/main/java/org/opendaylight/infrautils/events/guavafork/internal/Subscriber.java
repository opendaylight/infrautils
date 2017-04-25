/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendaylight.infrautils.events.guavafork.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

/**
 * A subscriber method on a specific object, plus the executor that should be used for dispatching
 * events to it.
 *
 * <p>Two subscribers are equivalent when they refer to the same method on the same object (not
 * class). This property is used to ensure that no subscriber method is registered more than once.
 *
 * @author Colin Decker
 */
class Subscriber {

  /**
   * Creates a {@code Subscriber} for {@code method} on {@code listener}.
   */
  static Subscriber create(EventBus bus, Object listener, Method method, Predicate<Method> isThreadSafe) {
    return isThreadSafe.test(method)
        ? new Subscriber(bus, listener, method)
        : new SynchronizedSubscriber(bus, listener, method);
  }

  /** The event bus this subscriber belongs to. */
  private final EventBus bus;

  /** The object with the subscriber method. */
  @VisibleForTesting final Object target;

  /** Subscriber method. */
  private final Method method;

  /** Executor to use for dispatching events to this subscriber. */
  private final Executor executor;

  private Subscriber(EventBus bus, Object target, Method method) {
    this.bus = bus;
    this.target = checkNotNull(target);
    this.method = method;
    method.setAccessible(true);

    this.executor = bus.executor();
  }

  /**
   * Dispatches {@code event} to this subscriber using the proper executor.
   */
  @SuppressWarnings("unchecked")
  final CompletableFuture<Void> dispatchEvent(final Object event) {
/*
      try {
        return (CompletableFuture<Object>) invokeSubscriberMethod(event);
      } catch (InvocationTargetException e) {
          bus.handleSubscriberException(e.getCause(), context(event));
          throw Throwables.propagate(e.getCause());
      } catch (IllegalArgumentException | IllegalAccessException e) {
          bus.handleSubscriberException(e, context(event));
          throw Throwables.propagate(e);
      }
 */
      return CompletableFuture.supplyAsync(() -> {
        try {
            return invokeSubscriberMethod(event);
        } catch (InvocationTargetException e) {
            bus.handleSubscriberException(e.getCause(), context(event));
            throw Throwables.propagate(e.getCause());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            bus.handleSubscriberException(e, context(event));
            throw Throwables.propagate(e);
        }
      }, executor);/*.handle((o, e) -> {
        if (e != null) {
            CompletableFuture<Void> exceptionallyCompletedFuture = new CompletableFuture<>();
            exceptionallyCompletedFuture.completeExceptionally(e);
            return exceptionallyCompletedFuture;
        } else if (o instanceof CompletableFuture) { // unwrap
            return CompletableFuture.completedFuture(((CompletableFuture<?>) o).join());
        } else {
            return CompletableFuture.completedFuture(o);
        }
    });*/
  }

  /**
   * Invokes the subscriber method. This method can be overridden to make the invocation
   * synchronized.
   */
  @VisibleForTesting
  Object invokeSubscriberMethod(Object event) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
//    try {
      return method.invoke(target, checkNotNull(event));
//    } catch (IllegalArgumentException e) {
//      throw new Error("Method rejected target/argument: " + event, e);
//    } catch (IllegalAccessException e) {
//      throw new Error("Method became inaccessible: " + event, e);

//    } catch (InvocationTargetException e) {
//      Throwable cause = e.getCause();
//      if (cause instanceof Error) {
//        throw (Error) cause;
//      }
//      throw e;
//    }
  }

  /**
   * Gets the context for the given event.
   */
  private SubscriberExceptionContext context(Object event) {
    return new SubscriberExceptionContext(bus, event, target, method);
  }

  @Override
  public final int hashCode() {
    return (31 + method.hashCode()) * 31 + System.identityHashCode(target);
  }

  @Override
  public final boolean equals(/*@Nullable*/ Object obj) {
    if (obj instanceof Subscriber) {
      Subscriber that = (Subscriber) obj;
      // Use == so that different equal instances will still receive events.
      // We only guard against the case that the same object is registered
      // multiple times
      return target == that.target && method.equals(that.method);
    }
    return false;
  }

  /**
   * Subscriber that synchronizes invocations of a method to ensure that only one thread may enter
   * the method at a time.
   */
  @VisibleForTesting
  static final class SynchronizedSubscriber extends Subscriber {

    private SynchronizedSubscriber(EventBus bus, Object target, Method method) {
      super(bus, target, method);
    }

    @Override
    Object invokeSubscriberMethod(Object event) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
      synchronized (this) {
        return super.invokeSubscriberMethod(event);
      }
    }
  }
}
