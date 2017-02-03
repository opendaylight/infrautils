/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.internal;

import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
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
 * MBeanService is a utility that can be used for registering a new MBean or accessing any MBean service.
 *
 * @author Faseela K
 */
public final class MBeanService {

    private static final Logger LOG = LoggerFactory.getLogger(MBeanService.class);

    public static void registerServerMBean(Object mxBeanImplementor, String objNameStr) {

        LOG.debug("register MXBean for {}", objNameStr);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName objName = new ObjectName(objNameStr);
            mbs.registerMBean(mxBeanImplementor, objName);
            LOG.info("MXBean registration for {} SUCCESSFUL.", objNameStr);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException
            | MalformedObjectNameException ex) {
            LOG.error("MXBean registration for {} FAILED due to {}", objNameStr, ex);
        }

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

    public static Object invokeMBeanFunction(String objName, String functionName, Object[] params) throws Exception {
        Object udpated = "";
        try {

            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();

            udpated = mplatformMbeanServer.invoke(objectName, functionName, params, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing MXBean function");
            throw t;
        }
        return udpated;
    }

}
