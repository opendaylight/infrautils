/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

/**
 * Labeled wrapper which "tags" T objects.
 *
 * @see MetricProvider
 *
 * @author Michael Vorburger.ch
 */
public interface Labeled<T> {

    T label(String labelValue);

}
