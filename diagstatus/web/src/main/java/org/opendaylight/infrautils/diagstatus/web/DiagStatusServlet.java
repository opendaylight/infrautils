/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;

/**
 * Web Servlet for diagstatus which returns JSON and HTTP status code.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Shut up FindBugs, because DiagStatusService is not Serializable.
    // It's not like we'll ever run this Servlet in a stupid distribution web container
    // which will actually serialize Servlet instances - that's so 90s WAS theoretical.
    @SuppressFBWarnings("SE_BAD_FIELD")
    private final DiagStatusService diagStatusService;

    public DiagStatusServlet(DiagStatusService diagStatusService) {
        this.diagStatusService = diagStatusService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.println(diagStatusService.getAllServiceDescriptorsAsJSON());
        printWriter.close();

        // use setStatus() NOT sendError(), because we are providing the response
        if (!diagStatusService.isOperational()) {
            // HTTP return code 418 instead of regular 200 is used so that scripts
            // who just want boolean status don't have to parse the JSON, if not interested.
            // see RFC-2324 & RFC-7168 for the meaning of HTTP return code 418 ...
            response.setStatus(418);
        }
    }

}
