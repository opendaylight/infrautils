/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.utils.management;

import java.lang.management.ManagementFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MBeanUtils is a utility that can be used for registering a new MBean or accessing any MBean service.
 *
 * @author Faseela K
 */
public final class MBeanUtils {

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
}
