/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.ops4j.pax.cdi.api.Service;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes and registers the {@link DiagStatusServlet}.
 *
 * <p>This implementation uses the OSGi {@link HttpService} directly, because our
 * WebContext and WebServer abstraction are in AAA, and we don't want to have a
 * dependency to AAA from infrautils.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class OsgiWebInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiWebInitializer.class);

    private static final String DIAGSTATUS_URL = "/diagstatus";

    private final HttpService osgiHttpService;

    @Inject
    public OsgiWebInitializer(@Service HttpService osgiHttpService,
            @Service DiagStatusService diagStatusService) throws ServletException, NamespaceException {
        this.osgiHttpService = osgiHttpService;
        osgiHttpService.registerServlet(DIAGSTATUS_URL, new DiagStatusServlet(diagStatusService), null, null);
        LOG.info("DiagStatus now exposed on: {}", DIAGSTATUS_URL);
    }

    @PreDestroy
    public void close() {
        osgiHttpService.unregister(DIAGSTATUS_URL);
    }
}
