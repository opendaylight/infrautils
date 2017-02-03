/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusManager is the core class having the functionalities for loading the configured services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
public class DiagStatusManager implements StatusMonitorServiceImplMBean {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusManager.class);
    public static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.services.status:type=SvcStatus";
    private java.util.Properties servicesMap = new java.util.Properties();

    @Inject
    public DiagStatusManager() {
        LOG.info("{} initialized", getClass().getSimpleName());
    }

    @PostConstruct
    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
        initializeStatusMonService();
    }

    public void initializeStatusMonService() {
        servicesMap = loadDiagStatusServiceProperties();
        initializeServices();
        registerMXBean();
    }

    @PreDestroy
    public void close() throws Exception {
        unregisterMXBean();
        LOG.info("{} close", getClass().getSimpleName());
    }

    private void registerMXBean() {
        LOG.debug("Register MXBean with Object-Name {}", JMX_OBJECT_NAME);
        MBeanService.registerServerMBean(this, JMX_OBJECT_NAME);
    }

    private void unregisterMXBean() {
        LOG.debug("Un-Register MXBean with Object-Name {}", JMX_OBJECT_NAME);
        MBeanService.unregisterServerMBean(this, JMX_OBJECT_NAME);
    }

    public Properties loadDiagStatusServiceProperties() {
        Properties prop = new Properties();
        java.io.InputStream input = null;
        try {
            String filename = "diagstatusservice.properties";
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                LOG.error("Unable to find " + filename);
                return null;
            }
            //load a properties file from class path
            prop.load(input);
        } catch (IOException ex) {
            LOG.error("Exception while reading properties:IOException");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error("Exception while reading properties");
                }
            }
        }
        return prop;
    }

    public void initializeServices(){
        // TODO
    }

    @Override
    public String acquireServiceStatusJSON(String outputType) {
        // TODO
        return null;
    }

    @Override
    public String acquireServiceStatus() {
        //TODO
        return null;
    }

    @Override
    public String acquireServiceStatusDetailed() {
        // TODO
        return null;
    }

    @Override
    public String acquireServiceStatusBrief() {
        // TODO
        return null;
    }

    @Override
    public java.util.HashMap acquireServiceStatusMAP() {
        // TODO
        return null;
    }

}
