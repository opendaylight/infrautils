/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.lang.management.ManagementFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
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

    // TODO Refactor this class, and move what is not diagstatus specific here into common/util
    //         into a new org.opendaylight.infrautils.utils.management package there

    private static final Logger LOG = LoggerFactory.getLogger(MBeanUtils.class);

    public static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";
    public static final String JMX_SVCSTATUS_OPERATION = "acquireServiceStatus";
    public static final String JMX_SVCSTATUS_OPERATION_DETAILED = "acquireServiceStatusDetailed";
    public static final String JMX_SVCSTATUS_OPERATION_BRIEF = "acquireServiceStatusBrief";
    public static final String JMX_SVCSTATUS_OPERATION_REMOTE = "acquireServiceStatusAsJSON";

    public static final String VERBOSE_OUTPUT_FORMAT = "V";
    public static final String BRIEF_OUPUT_FORMAT = "B";
    public static final String DEBUG_OUTPUT_FORMAT = "D";

    private MBeanUtils() {
    }

    public static void registerServerMBean(Object mxBeanImplementor, String objNameStr) throws JMException {
        LOG.debug("register MXBean for {}", objNameStr);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        ObjectName objName = new ObjectName(objNameStr);
        mbs.registerMBean(mxBeanImplementor, objName);
        LOG.info("MXBean registration for {} SUCCESSFUL.", objNameStr);
    }

    public static void unregisterServerMBean(Object mxBeanImplementor, String objNameStr) {
        LOG.debug("unregister MXBean for {}", objNameStr);
        MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mplatformMbeanServer.unregisterMBean(new ObjectName(objNameStr));
        } catch (InstanceNotFoundException | MalformedObjectNameException | MBeanRegistrationException e) {
            LOG.error("Error while unregistering MXBean {}", objNameStr, e);
        }
    }

    public static Object invokeMBeanFunction(String objName, String functionName) {
        Object udpated = "";
        try {

            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();
            udpated = mplatformMbeanServer.invoke(objectName, functionName, null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing MXBean function");
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
