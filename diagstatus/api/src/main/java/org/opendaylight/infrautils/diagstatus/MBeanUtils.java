/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MBeanUtils is a utility that can be used for registering a new MBean or accessing any MBean service.
 *
 * @author Faseela K
 */
public final class MBeanUtils {

    // TODO Refactor this class, and move what is not diagstatus specific here into common/util
    //         into a new org.opendaylight.infrautils.utils.management package there

    private static final Logger LOG = LoggerFactory.getLogger(MBeanUtils.class);

    public static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";
    public static final String JMX_SVCSTATUS_OPERATION_DETAILED = "acquireServiceStatusDetailed";
    public static final String JMX_URL_PREFIX = "service:jmx:rmi:///jndi/rmi://";
    public static final String JMX_URL_SUFFIX = "/server";
    public static final String JMX_URL_SEPARATOR = ":";
    public static final int RMI_REGISTRY_PORT = 6886;

    private MBeanUtils() {
    }

    public static JMXServiceURL getJMXUrl(String host) throws MalformedURLException {
        String jmxUrl = constructJmxUrl(host, RMI_REGISTRY_PORT);
        return new JMXServiceURL(jmxUrl);
    }

    private static String constructJmxUrl(String host, int port) {
        return new StringBuilder().append(JMX_URL_PREFIX).append(host).append(JMX_URL_SEPARATOR).append(port)
              .append(JMX_URL_SUFFIX).toString();
    }

    public static MBeanServer registerServerMBean(Object mxBeanImplementor, String objNameStr)
            throws JMException {

        LOG.debug("register MBean for {}", objNameStr);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objName = new ObjectName(objNameStr);
            mbs.registerMBean(mxBeanImplementor, objName);
            LOG.info("MBean registration for {} SUCCESSFUL.", objNameStr);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
            | MalformedObjectNameException ex) {
            LOG.error("MBean registration for {} FAILED.", objNameStr, ex);
            throw ex;
        }
        return mbs;
    }

    public static Object invokeMBeanFunction(String objName, String functionName) {
        Object udpated = "";
        try {
            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            udpated = mplatformMbeanServer.invoke(objectName, functionName, null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing MBean function");
        }
        return udpated;
    }

    public static Object readMBeanAttribute(String objName, String attribute) {
        Object attributeObj = null;
        try {
            ObjectName objectName = new ObjectName(objName);
            MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            attributeObj = platformMbeanServer.getAttribute(objectName, attribute);
        } catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException
                | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing MXBean function");
        }
        return attributeObj;
    }

    public static String invokeRemoteJMXOperation(String host, String mbeanName) throws Exception {
        JMXServiceURL url = getJMXUrl(host);
        String serviceStatus;
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName mbeanObj = new ObjectName(mbeanName);
        // Create a dedicated proxy for the MBean instead of
        // going directly through the MBean server connection
        try {
            DiagStatusServiceMBean mbeanProxy =
                    JMX.newMBeanProxy(mbsc, mbeanObj, DiagStatusServiceMBean.class, true);
            serviceStatus = mbeanProxy.acquireServiceStatusDetailed();
        } finally {
            jmxc.close();
        }
        return serviceStatus;
    }
}
