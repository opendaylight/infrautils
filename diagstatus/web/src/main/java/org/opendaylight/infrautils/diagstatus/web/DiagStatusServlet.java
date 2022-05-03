/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

/**
 * Web Servlet for diagstatus which returns JSON and HTTP status code.
 *
 * @author Michael Vorburger.ch
 */
// FIXME: @WebServlet?
@HttpWhiteboardServletPattern("/diagstatus")
@HttpWhiteboardServletName("DiagStatusServlet")
@Component(service = Servlet.class)
public final class DiagStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Suppress FindBugs warning, because DiagStatusService is not Serializable;
    // it's not like we'll ever run this Servlet in a distributed web container which
    // will actually serialize Servlet instances - that's so 90s IBM WAS theoretical! ;)
    @SuppressFBWarnings("SE_BAD_FIELD")
    private final DiagStatusService diagStatusService;

    @Activate
    public DiagStatusServlet(@Reference DiagStatusService diagStatusService) {
        this.diagStatusService = requireNonNull(diagStatusService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        // use setStatus() NOT sendError(), because we are providing the response
        // INFRAUTILS-47: MUST use setStatus() *BEFORE* response.getWriter()

        ServiceStatusSummary status = diagStatusService.getServiceStatusSummary();
        if (!status.isOperational()) {
            // HTTP return code 503 instead of regular 200 is used so that scripts
            // who just want boolean status don't have to parse the JSON, if not interested.
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        try (var printWriter = response.getWriter()) {
            printWriter.println(status.toJSON());
        }
    }
}
