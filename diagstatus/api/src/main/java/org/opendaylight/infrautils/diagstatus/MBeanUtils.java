/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import static java.util.Objects.requireNonNull;

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
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
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

    public static String constructJmxUrl(@Var String targetHost, int rmiRegistryPort) {
        if (isIpv6Address(targetHost)) {
            targetHost = '[' + targetHost + ']';
        }
        return JMX_HOST_PREFIX + targetHost + JMX_TARGET_PREFIX
                + targetHost + JMX_URL_SEPARATOR + rmiRegistryPort + JMX_URL_SUFFIX;
    }

    public static Pair<JMXConnectorServer,Registry> startRMIConnectorServer(MBeanServer mbeanServer, String selfAddress)
            throws IOException {
        JMXServiceURL url = getJMXUrl(requireNonNull(selfAddress, "selfAddress"));
        Registry registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
        JMXConnectorServer cs;
        try {
            cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, requireNonNull(mbeanServer, "mbeanServer"));
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

    @Nullable
    public static Object getMBeanAttribute(String objName, String attribute) throws JMException {
        ObjectName objectName = new ObjectName(objName);
        MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        return platformMbeanServer.getAttribute(objectName, attribute);
    }

    private static <T> T getMBean(String jmxName, Class<T> klass, MBeanServerConnection mbsc)
            throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(jmxName);
        if (JMX.isMXBeanInterface(klass)) {
            return JMX.newMXBeanProxy(mbsc, objectName, klass);
        } else {
            return JMX.newMBeanProxy(mbsc, objectName, klass);
        }
    }

    public static <T> T getMBean(String jmxName, Class<T> klass) throws MalformedObjectNameException {
        return getMBean(jmxName, klass, ManagementFactory.getPlatformMBeanServer());
    }

    public static <T, R> R invokeRemoteMBeanOperation(String remoteURL, String jmxName, Class<T> klass,
            Function<T, R> function) throws MalformedObjectNameException, IOException {
        JMXServiceURL jmxURL = new JMXServiceURL(remoteURL);
        try (JMXConnector jmxc = JMXConnectorFactory.connect(jmxURL, null)) {
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            T remoteMBean = getMBean(jmxName, klass, mbsc);
            return function.apply(remoteMBean);
        }
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
