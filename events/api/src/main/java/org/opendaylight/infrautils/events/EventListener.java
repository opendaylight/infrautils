/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Listener for event messages (AKA "handler").
 *
 * @param <E> Event type (note that there is no Event base type / marker interface)
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_INTERFACE")
public interface EventListener<E> extends java.util.EventListener {

    void on(E event) throws Exception;

}
