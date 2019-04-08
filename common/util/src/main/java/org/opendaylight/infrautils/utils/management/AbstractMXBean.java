/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.management;

import com.google.errorprone.annotations.Var;
import java.lang.management.ManagementFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is inspired from the original implementation in controller.
 *
 * @author Thomas Pantelis
 * @author Faseela K
 */
public abstract class AbstractMXBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMXBean.class);

    public static final String BASE_JMX_PREFIX = "org.opendaylight.infrautils:";

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final String mbeanName;
    private final String mbeanType;
    private final @Nullable String mbeanCategory;

    /**
     * Constructor.
     *
     * @param mbeanName Used as the <code>name</code> property in the bean's ObjectName.
     * @param mbeanType Used as the <code>type</code> property in the bean's ObjectName.
     * @param mbeanCategory Used as the <code>Category</code> property in the bean's ObjectName.
     */
    protected AbstractMXBean(@NonNull String mbeanName, @NonNull String mbeanType,
                             @Nullable String mbeanCategory) {
        this.mbeanName = mbeanName;
        this.mbeanType = mbeanType;
        this.mbeanCategory = mbeanCategory;
    }

    private ObjectName getMBeanObjectName() throws MalformedObjectNameException {
        StringBuilder builder = new StringBuilder(BASE_JMX_PREFIX)
                .append("type=").append(getMBeanType());
        if (getMBeanCategory() != null) {
            builder.append(",Category=").append(getMBeanCategory());
        }
        builder.append(",name=").append(getMBeanName());
        return new ObjectName(builder.toString());
    }

    /**
     * This method is a wrapper for registerMBean with void return type so it can be invoked by dependency
     * injection frameworks such as Spring and Blueprint.
     */
    public void register() {
        registerMBean();
    }

    /**
     * Registers this bean with the platform MBean server with the domain defined by
     * {@link #BASE_JMX_PREFIX}.
     *
     * @return true is successfully registered, false otherwise.
     */
    public final boolean registerMBean() {
        @Var boolean registered = false;
        try {
            // Object to identify MBean
            ObjectName mbeanObjectName = this.getMBeanObjectName();
            LOG.debug("Register MBean {}", mbeanObjectName);
            // unregistered if already registered
            if (server.isRegistered(mbeanObjectName)) {
                LOG.debug("MBean {} found to be already registered", mbeanObjectName);
                try {
                    unregisterMBean(mbeanObjectName);
                } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                    LOG.warn("unregister mbean {} caused exception", mbeanObjectName, e);
                }
            }
            server.registerMBean(this, mbeanObjectName);
            registered = true;
            LOG.debug("MBean {} registered successfully", mbeanObjectName.getCanonicalName());
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
                | MalformedObjectNameException e) {
            LOG.error("MBean {} registration failed", mbeanName, e);
        }
        return registered;
    }

    /**
     * This method is a wrapper for unregisterMBean with void return type so it can be invoked by dependency
     * injection frameworks such as Spring and Blueprint.
     */
    public void unregister() {
        unregisterMBean();
    }

    /**
     * Unregisters this bean with the platform MBean server.
     *
     * @return true is successfully unregistered, false otherwise.
     */
    public boolean unregisterMBean() {
        @Var boolean unregister = false;
        @Var ObjectName mbeanobjectname = null;
        try {
            mbeanobjectname = this.getMBeanObjectName();
            unregisterMBean(mbeanobjectname);
            unregister = true;
        } catch (InstanceNotFoundException | MBeanRegistrationException
                | MalformedObjectNameException e) {
            LOG.debug("Failed when unregistering MBean {}", mbeanobjectname, e);
        }
        return unregister;
    }

    private void unregisterMBean(ObjectName mbeanObjectName) throws MBeanRegistrationException,
            InstanceNotFoundException {
        server.unregisterMBean(mbeanObjectName);
    }

    /**
     * invoke an mbean function with the platform MBean server.
     *
     * @return Object if successfully executed, "" otherwise.
     */
    public Object invokeMBeanFunction(String functionName) {
        @Var Object result = "";
        @Var ObjectName objectName = null;
        try {
            objectName = this.getMBeanObjectName();
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            result = mplatformMbeanServer.invoke(objectName, functionName, null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException e) {
            LOG.error("Failed when executing MBean function {} in {}", functionName, objectName, e);
        }
        return result;
    }

    /**
     * Read an mbean attribute from the platform MBean server.
     *
     * @return Object if successfully executed, "" otherwise.
     */
    public Object readMBeanAttribute(String attribute) {
        @Var Object attributeObj = "";
        @Var ObjectName objectName = null;
        try {
            objectName = this.getMBeanObjectName();
            MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            attributeObj = platformMbeanServer.getAttribute(objectName, attribute);
        } catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException
                | ReflectionException | MalformedObjectNameException e) {
            LOG.info("Failed when reading MBean attribute {} from {}", attribute, objectName, e);
        }
        return attributeObj;
    }

    /**
     * Returns the <code>name</code> property of the bean's ObjectName.
     */
    public String getMBeanName() {
        return mbeanName;
    }

    /**
     * Returns the <code>type</code> property of the bean's ObjectName.
     */
    public String getMBeanType() {
        return mbeanType;
    }

    /**
     * Returns the <code>Category</code> property of the bean's ObjectName.
     */
    public @Nullable String getMBeanCategory() {
        return mbeanCategory;
    }
}
