/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.utils.management;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.lang3.tuple.Pair;
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
    public static final String JMX_URL_PREFIX = "service:jmx:rmi:///jndi/rmi://";
    public static final String JMX_URL_SUFFIX = "/server";
    public static final String JMX_URL_SEPARATOR = ":";
    public static final int RMI_REGISTRY_PORT = 6886;

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final String mbeanName;
    private final String mbeanType;
    private final String mbeanCategory;

    /**
     * Constructor.
     *
     * @param mbeanName Used as the <code>name</code> property in the bean's ObjectName.
     * @param mbeanType Used as the <code>type</code> property in the bean's ObjectName.
     * @param mbeanCategory Used as the <code>Category</code> property in the bean's ObjectName.
     */
    protected AbstractMXBean(@Nonnull String mbeanName, @Nonnull String mbeanType,
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
    public boolean registerMBean() {
        boolean registered = false;
        try {
            // Object to identify MBean
            final ObjectName mbeanName = this.getMBeanObjectName();
            LOG.debug("Register MBean {}", mbeanName);
            // unregistered if already registered
            if (server.isRegistered(mbeanName)) {
                LOG.debug("MBean {} found to be already registered", mbeanName);
                try {
                    unregisterMBean(mbeanName);
                } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                    LOG.warn("unregister mbean {} resulted in exception {} ", mbeanName, e);
                }
            }
            server.registerMBean(this, mbeanName);
            registered = true;
            LOG.debug("MBean {} registered successfully", mbeanName.getCanonicalName());
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
        boolean unregister = false;
        try {
            ObjectName mbeanName = this.getMBeanObjectName();
            unregisterMBean(mbeanName);
            unregister = true;
        } catch (InstanceNotFoundException | MBeanRegistrationException
                | MalformedObjectNameException e) {
            LOG.debug("Failed when unregistering MBean {}", e);
        }
        return unregister;
    }

    private void unregisterMBean(ObjectName mbeanName) throws MBeanRegistrationException,
            InstanceNotFoundException {
        server.unregisterMBean(mbeanName);
    }

    /**
     * invoke an mbean function with the platform MBean server.
     *
     * @return Object if successfully executed, "" otherwise.
     */
    public Object invokeMBeanFunction(String functionName) {
        Object result = "";
        try {
            ObjectName objectName = this.getMBeanObjectName();
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            result = mplatformMbeanServer.invoke(objectName, functionName, null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException e) {
            LOG.error("Failed when executing MBean function", e);
        }
        return result;
    }

    /**
     * read an mbean attribute from the platform MBean server.
     *
     * @return Object if successfully executed, "" otherwise.
     */
    public Object readMBeanAttribute(String attribute) {
        Object attributeObj = "";
        try {
            ObjectName objectName = this.getMBeanObjectName();
            MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            attributeObj = platformMbeanServer.getAttribute(objectName, attribute);
        } catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException
                | ReflectionException | MalformedObjectNameException e) {
            LOG.info("Failed when reading MBean attribute", e);
        }
        return attributeObj;
    }

    public static JMXServiceURL getJMXUrl(String host) throws MalformedURLException {
        String jmxUrl = constructJmxUrl(host, RMI_REGISTRY_PORT);
        return new JMXServiceURL(jmxUrl);
    }

    private static String constructJmxUrl(String host, int port) {
        return new StringBuilder().append(JMX_URL_PREFIX).append(host).append(JMX_URL_SEPARATOR).append(port)
                .append(JMX_URL_SUFFIX).toString();
    }

    public Pair startRMIConnectorServer(String selfAddress) throws IOException {
        JMXServiceURL url = getJMXUrl(selfAddress);
        Registry registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
        JMXConnectorServer cs;
        try {
            cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
            cs.start();
        } catch (IOException e) {
            LOG.error("Error while trying to create new JMX Connector for url {}", url, e);
            throw e;
        }
        LOG.info("JMX Connector Server started for url {}", url);
        return Pair.of(cs, registry);
    }

    public static void stopRMIConnectorServer(Pair<JMXConnectorServer, Registry> jmxConnector) throws IOException {
        try {
            jmxConnector.getLeft().stop();
            LOG.info("JMX Connector Server stopped {}", jmxConnector);
            UnicastRemoteObject.unexportObject(jmxConnector.getRight(), true);
        } catch (IOException e) {
            LOG.error("Error while trying to stop jmx connector server {}", jmxConnector);
            throw e;
        }
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
    public String getMBeanCategory() {
        return mbeanCategory;
    }
}
