/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.management;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is inspired from the original implementation in controller.
 *
 * @author Thomas Pantelis
 * @author Faseela K
 */
public abstract class AbstractMXBean {

    private final MXBeanSupport support;

    /**
     * Constructor.
     *
     * @param mbeanName Used as the <code>name</code> property in the bean's ObjectName.
     * @param mbeanType Used as the <code>type</code> property in the bean's ObjectName.
     * @param mbeanCategory Used as the <code>Category</code> property in the bean's ObjectName.
     */
    protected AbstractMXBean(@Nonnull String mbeanName, @Nonnull String mbeanType,
                             @Nullable String mbeanCategory) {
        support = new MXBeanSupport(mbeanName, mbeanType, mbeanCategory);
    }

    /**
     * This method is a wrapper for registerMBean with void return type so it can be invoked by dependency
     * injection frameworks such as Spring and Blueprint.
     */
    public void register() {
        support.registerMBean();
    }

    /**
     * This method is a wrapper for unregisterMBean with void return type so it can be invoked by dependency
     * injection frameworks such as Spring and Blueprint.
     */
    public void unregister() {
        support.unregisterMBean();
    }
}
