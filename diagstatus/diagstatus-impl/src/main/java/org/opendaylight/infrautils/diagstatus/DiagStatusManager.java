/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.infrautils.diagstatus.api.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusManager is the core class having the functionality for loading the configured services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
public class DiagStatusManager implements DiagStatusService {

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
        MBeanService.registerServerMBean(this, JMX_OBJECT_NAME);
    }

    @PreDestroy
    public void close() throws Exception {
        MBeanService.unregisterServerMBean(this, JMX_OBJECT_NAME);
        LOG.info("{} close", getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<Void> register(String service) {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public Service getServiceState(String serviceIdentifier) {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public List<Service> getAllServiceStates() {
        //TODO will come in subsequent patches
        return null;
    }
}
