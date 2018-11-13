/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.lang.management.ManagementFactory;
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

    @Nullable
    public static Object getMBeanAttribute(String objName, String attribute) throws JMException {
        ObjectName objectName = new ObjectName(objName);
        MBeanServer platformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        return platformMbeanServer.getAttribute(objectName, attribute);
    }
}
