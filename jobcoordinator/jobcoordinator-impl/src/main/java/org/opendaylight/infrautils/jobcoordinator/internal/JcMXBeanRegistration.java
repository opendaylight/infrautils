/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.jobcoordinator.internal;

import java.lang.management.ManagementFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JcMXBeanRegistration {

    private final MBeanServer mbeanServer;
    private final JcServiceStatus jcServiceStatus;
    private static final String OBJ_ID = "org.opendaylight.infrautils.jobcoordinator.internal:type=JcServiceStatus";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    @SuppressWarnings("checkstyle:IllegalCatch")
    public JcMXBeanRegistration(JcServiceStatus jcServiceStatus) {

        this.jcServiceStatus = jcServiceStatus;
        mbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            createMXBeanForJcService();
        } catch (Exception e) {
            log.error("jcMXBeanRegistration failed",e);
        }
    }

    private void createMXBeanForJcService() throws Exception {

        if (mbeanServer != null) {
            try {
                mbeanServer.registerMBean(jcServiceStatus, new ObjectName(OBJ_ID));
                log.info("jcServiceStatus registration done successfully");
            } catch (InstanceAlreadyExistsException iaee) {
                mbeanServer.unregisterMBean(new ObjectName(OBJ_ID));
                log.error("jcServiceStatus registration done unsuccessfully");
                mbeanServer.registerMBean(jcServiceStatus, new ObjectName(OBJ_ID));
            }
        }
    }


}
