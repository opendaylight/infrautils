/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.lang.management.ManagementFactory;
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
import org.eclipse.jdt.annotation.Nullable;
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

    private MBeanUtils() {
    }

    public static MBeanServer registerServerMBean(Object mxBeanImplementor, ObjectName objName)
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        LOG.debug("register MBean for {}", objName);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(mxBeanImplementor, objName);
            LOG.info("MBean registration for {} SUCCESSFUL.", objName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
            LOG.error("MBean registration for {} FAILED.", objName, ex);
            throw ex;
        }
        return mbs;
    }

    public static MBeanServer registerServerMBean(Object mxBeanImplementor, String objNameStr)
            throws JMException {
        return registerServerMBean(mxBeanImplementor, ObjectName.getInstance(objNameStr));
    }

    public static void unregisterServerMBean(Object mxBeanImplementor, ObjectName objName)
            throws InstanceNotFoundException, MBeanRegistrationException {
        LOG.debug("unregister MXBean for {}", objName);
        MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mplatformMbeanServer.unregisterMBean(objName);
        } catch (InstanceNotFoundException | MBeanRegistrationException e) {
            LOG.error("Error while unregistering MBean {}", objName, e);
            throw e;
        }
    }

    public static void unregisterServerMBean(Object mxBeanImplementor, String objNameStr)
            throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
        unregisterServerMBean(mxBeanImplementor, ObjectName.getInstance(objNameStr));
    }

    public static @Nullable Object getMBeanAttribute(String objName, String attribute) throws JMException {
        return ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName(objName), attribute);
    }

    private static <T> T getMBean(String jmxName, Class<T> klass, MBeanServerConnection mbsc)
            throws MalformedObjectNameException {
        var objectName = new ObjectName(jmxName);
        return JMX.isMXBeanInterface(klass) ? JMX.newMXBeanProxy(mbsc, objectName, klass)
                : JMX.newMBeanProxy(mbsc, objectName, klass);
    }

    public static <T> T getMBean(String jmxName, Class<T> klass) throws MalformedObjectNameException {
        return getMBean(jmxName, klass, ManagementFactory.getPlatformMBeanServer());
    }

    static ObjectName objectNameOf(String name) {
        try {
            return ObjectName.getInstance(name);
        } catch (MalformedObjectNameException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
