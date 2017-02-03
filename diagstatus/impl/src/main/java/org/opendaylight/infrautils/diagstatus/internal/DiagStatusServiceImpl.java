/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.infrautils.diagstatus.internal;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceDescriptor;
import org.opendaylight.infrautils.diagstatus.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DiagStatusServiceImpl is the core class having the functionality for loading the configured services
 * and aggregating the status of the same.
 * @author Faseela K
 */
@Singleton
public class DiagStatusServiceImpl implements DiagStatusService, DiagStatusServiceImplMBean {

    private static final Logger LOG = LoggerFactory.getLogger(DiagStatusServiceImpl.class);
    public static final String JMX_OBJECT_NAME = "org.opendaylight.infrautils.diagstatus:type=SvcStatus";

    @Inject
    public DiagStatusServiceImpl() {
        LOG.info("{} initialized", getClass().getSimpleName());
    }

    @PostConstruct
    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
        initializeStatusMonService();
    }

    public void initializeStatusMonService() {
        MBeanUtils.registerServerMBean(this, JMX_OBJECT_NAME);
    }

    @PreDestroy
    public void close() throws Exception {
        MBeanUtils.unregisterServerMBean(this, JMX_OBJECT_NAME);
        LOG.info("{} close", getClass().getSimpleName());
    }

    @Override
    public CompletableFuture<Void> register(String service) {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public void report(String service, ServiceState serviceState) {
        //TODO will come in subsequent patches
    }

    @Override
    public ServiceDescriptor getServiceDescriptor(String serviceIdentifier) {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public List<ServiceDescriptor> getAllServiceDescriptors() {
        //TODO will come in subsequent patches
        return null;
    }

    /*
     *  MBean interface Implementations for acquiring service status in a cluster wide manner.
     */
    @Override
    public String acquireServiceStatus() {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public String acquireServiceStatusDetailed() {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public String acquireServiceStatusBrief() {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public String acquireServiceStatusJSON(String outputType) {
        //TODO will come in subsequent patches
        return null;
    }

    @Override
    public HashMap acquireServiceStatusMAP() {
        //TODO will come in subsequent patches
        return null;
    }
}
