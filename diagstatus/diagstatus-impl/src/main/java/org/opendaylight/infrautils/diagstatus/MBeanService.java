/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
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
public class MBeanService {

    private static final Logger LOG = LoggerFactory.getLogger(MBeanService.class);

    public static void registerServerMBean(Object mxBeanImplementor, String objNameStr) {

        LOG.debug("MXBean registration task starting");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName objName = new ObjectName(objNameStr);
            LOG.debug("MXBean Object-Name framed");
            mbs.registerMBean(mxBeanImplementor, objName);
            LOG.info("MXBean registration SUCCESSFUL!!!");
        } catch (InstanceAlreadyExistsException iaeEx) {
            LOG.error("MXBean registration FAILED with InstanceAlreadyExistsException", iaeEx);
        } catch (MBeanRegistrationException mbrEx) {
            LOG.error("MXBean registration FAILED with MBeanRegistrationException", mbrEx);
        } catch (NotCompliantMBeanException ncmbEx) {
            LOG.error("MXBean registration FAILED with NotCompliantMBeanException", ncmbEx);
        } catch (MalformedObjectNameException monEx) {
            LOG.error("MXBean registration failed with MalformedObjectNameException", monEx);
        }

    }

    public static void unregisterServerMBean(Object mxBeanImplementor, String objNameStr) {
        MBeanServer mplatformMbeanServer;

        mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            mplatformMbeanServer.unregisterMBean(new ObjectName(objNameStr));
        } catch (InstanceNotFoundException | MalformedObjectNameException | MBeanRegistrationException e) {
            LOG.error("Error while unregistering ITM Mbean", e);
        }
    }

    public static Object invokeMBeanFunction(String objName, String functionName, Object[] params) throws Exception {
        Object udpated = "";
        MBeanOperationInfo[] operations = null;
        try {

            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();

            udpated = (Object) mplatformMbeanServer.invoke(objectName, functionName, params, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException t) {
            LOG.info("CRITICAL : Exception in executing mbean function");
            throw t;
        }
        return udpated;
    }

    public static Object invokeMBeanFunction(String objName, String functionName, Properties params, String
        signature) throws Exception {
        Object udpated = "";
        MBeanOperationInfo[] operations = null;
        try {

            ObjectName objectName = new ObjectName(objName);
            MBeanServer mplatformMbeanServer = ManagementFactory.getPlatformMBeanServer();

            udpated = (Object) mplatformMbeanServer.invoke(objectName, functionName, new Object[] {params}, new
                String[] {signature});

        } catch (InstanceNotFoundException | MBeanException | MalformedObjectNameException | ReflectionException t) {
            LOG.info("CRITICAL : Exception in executing mbean function");
            throw t;
        }
        return udpated;
    }

}
