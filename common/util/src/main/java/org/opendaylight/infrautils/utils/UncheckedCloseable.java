/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils;

/**
 * Something which can be {@link #close()}'d.
 *
 * <p>Extends {@link AutoCloseable} but the {@link #close()} here does not throw any Exception.
 *
 * <p>
 * Used e.g. by APIs of OSGi bundles which return objects that must be closed
 * either when such objects are no longer required, or when unloading the
 * bundle; typically e.g. in Blueprint beans' {@literal @}PreDestroy methods.
 * The infrautils metrics are an example of such objects.
 *
 * @author Michael Vorburger.ch
 * @deprecated Use {@code org.opendaylight.yangtools.concepts.Registration} instead.
 */
@Deprecated(since = "2.0.7", forRemoval = true}
public interface UncheckedCloseable extends AutoCloseable {

    @Override
    void close();
}
