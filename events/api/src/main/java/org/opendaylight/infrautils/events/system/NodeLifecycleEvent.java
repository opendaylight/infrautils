/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.events.system;

/**
 * Base type for node system events.
 *
 * <p>The "node" here can be a single Karaf instance which may or may not be part of a cluster,
 * or simply a standalone JVM e.g. in a test environment, in which case these events may be
 * fired related to the context of a dependency injection framework such as e.g. Guice.
 *
 * @author Michael Vorburger.ch
 */
public interface NodeLifecycleEvent { }
