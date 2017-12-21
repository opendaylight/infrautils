/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.metrics;

/**
 * Metric which can be {@link #close()}'d.
 *
 * <p>OSGi bundles using metrics must close() them when unloading the bundle
 * (typically e.g. via Blueprint {@literal @}PreDstroy), or if they are dynamic
 * metrics (e.g. for devices) when they are no longer needed.
 *
 * <p>Metrics that are closed can no longer be used, and will throw an IllegalStateException if they are.
 *
 * @author Michael Vorburger.ch
 */
public interface CloseableMetric {

    void close();

}
