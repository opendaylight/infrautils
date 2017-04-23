/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event subscriber (see {@link EventListener}).
 *
 * <p>The type of event will be indicated by the method's first (and only)
 * parameter.  If this annotation is applied to methods with zero parameters,
 * or more than one parameter, then registering with {@link EventSubscriber#subscribe(Object)}
 * will throw an {@link IllegalArgumentException}.  Annotated methods may throws any exceptions.
 * By convention, such methods typically have signatures like <code>onYourEventType(YourEventType event)</code>.
 *
 * @author Michael Vorburger.ch
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
}
