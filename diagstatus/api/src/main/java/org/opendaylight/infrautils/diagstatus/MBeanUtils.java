/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import com.google.common.net.InetAddresses;
import com.google.errorprone.annotations.Var;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.annotation.Nullable;
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
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MBeanUtils is a utility that can be used for registering a new MBean or accessing any MBean service.
 *
 * @author Faseela K - initial author
 * @author Michael Vorburger.ch - exception handling improvements and introduction of strongly typed getMBean()
 */
public final class MBeanUtils {

    // TODO Refactor this class, and move what is not diagstatus specific here into common/util
    //         into a new org.opendaylight.infrautils.utils.management package there

    private static final Logger LOG = LoggerFactory.getLogger(MBeanUtils.class);

    public static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";
    public static final String JMX_SVCSTATUS_OPERATION_DETAILED = "acquireServiceStatusDetailed";
    public static final String JMX_HOST_PREFIX = "service:jmx:rmi://";
    public static final String JMX_TARGET_PREFIX = "/jndi/rmi://";
    public static final String JMX_URL_SUFFIX = "/server";
    public static final String JMX_URL_SEPARATOR = ":";
    public static final int RMI_REGISTRY_PORT = 6886;

    private MBeanUtils() {
    }

    public static JMXServiceURL getJMXUrl(String targetHost) throws MalformedURLException {
        String jmxUrl = constructJmxUrl(targetHost, RMI_REGISTRY_PORT);
        return new JMXServiceURL(jmxUrl);
    }

    private static String constructJmxUrl(@Var String targetHost, int rmiRegistryPort) {
        if (isIpv6Address(targetHost)) {
            targetHost = '[' + targetHost + ']';
        }
        return JMX_HOST_PREFIX + targetHost + JMX_TARGET_PREFIX
                + targetHost + JMX_URL_SEPARATOR + rmiRegistryPort + JMX_URL_SUFFIX;
    }

    public static Pair<JMXConnectorServer,Registry> startRMIConnectorServer(MBeanServer mbeanServer, String selfAddress)
            throws IOException {
        JMXServiceURL url = getJMXUrl(selfAddress);
        Registry registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
        JMXConnectorServer cs;
        try {
            cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
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

    public static void unregisterServerMBean(Object mxBeanImplementor, String objNameStr)
            throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
        LOG.debug("unregister MXBean for {}", objNameStr);
        MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mplatformMbeanServer.unregisterMBean(new ObjectName(objNameStr));
        } catch (InstanceNotFoundException | MalformedObjectNameException | MBeanRegistrationException e) {
            LOG.error("Error while unregistering MBean {}", objNameStr, e);
            throw e;
        }
    }

    public static Object invokeMBeanFunction(String objName, String functionName) {
        @Var Object udpated = "";
        try {
            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            udpated = mplatformMbeanServer.invoke(objectName, functionName, null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing MBean function");
        }
        return udpated;
    }

    @Nullable
    public static Object getMBeanAttribute(String objName, String attribute) throws JMException {
        ObjectName objectName = new ObjectName(objName);
        MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        return platformMbeanServer.getAttribute(objectName, attribute);
    }

    /**
     * TODO remove this as soon as the usage in
     * org.opendaylight.genius.mdsalutil.diagstatus.internal.DatastoreServiceStatusProvider.getServiceDescriptor()
     * is removed.
     *
     * @deprecated Use {@link #getMBeanAttribute(String, String)} instead.
     */
    @Nullable
    @Deprecated
    public static Object readMBeanAttribute(String objName, String attribute) {
        @Var Object attributeObj = null;
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

    public static <T> T getMBean(String jmxName, Class<T> klass) throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(jmxName);
        MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        if (JMX.isMXBeanInterface(klass)) {
            return JMX.newMXBeanProxy(platformMbeanServer, objectName, klass);
        } else {
            return JMX.newMBeanProxy(platformMbeanServer, objectName, klass);
        }
    }

    public static String invokeRemoteJMXOperation(String host, String mbeanName) throws Exception {
        JMXServiceURL url = getJMXUrl(host);
        LOG.info("jmx service url generated : {}", url);
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

    private static Boolean isIpv6Address(String ipAddress) {
        try {
            InetAddress address = InetAddresses.forString(ipAddress);
            return address instanceof Inet6Address;
        } catch (IllegalArgumentException e) {
            String msg = "IllegalArgumentException while checking whether '" + ipAddress + "' is an IPv6 address";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
