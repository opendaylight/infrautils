/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.opendaylight.infrautils.ready.SystemState.ACTIVE;
import static org.opendaylight.infrautils.ready.SystemState.BOOTING;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.opendaylight.infrautils.testutils.mockito.MoreAnswers;
import org.opendaylight.infrautils.testutils.web.TestWebClient;
import org.opendaylight.infrautils.testutils.web.TestWebClient.Method;
import org.opendaylight.infrautils.testutils.web.TestWebServer;

/**
 * Test for {@link DiagStatusServlet}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusServletTest {
    private TestWebServer webServer;
    private TestWebClient webClient;
    private final TestDiagStatusService testDiagStatusService =
        mock(TestDiagStatusService.class, MoreAnswers.realOrException());

    @Before
    public void beforeTest() throws ServletException {
        webServer = new TestWebServer();
        webClient = new TestWebClient(webServer);
        webServer.registerServlet(new DiagStatusServlet(testDiagStatusService), "/*");
    }

    @After
    public void afterTest() throws ServletException {
        webServer.close();
    }

    @Test
    public void testGetWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertEquals(200, getDiagStatusResponseCode(Method.GET));
    }

    @Test
    public void testHeadWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertEquals(200, getDiagStatusResponseCode(Method.HEAD));
    }

    @Test
    public void testGetWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertEquals(503, getDiagStatusResponseCode(Method.GET));
    }

    @Test
    public void testHeadWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertEquals(503, getDiagStatusResponseCode(Method.HEAD));
    }

    private int getDiagStatusResponseCode(Method httpMethod) throws IOException {
        return webClient.request(httpMethod, "").getStatus();
    }

    private abstract static class TestDiagStatusService implements DiagStatusService {

        Boolean isOperational;

        @Override
        public ServiceStatusSummary getServiceStatusSummary() {
            return new ServiceStatusSummary(isOperational, isOperational ? ACTIVE : BOOTING, "", Set.of());
        }
    }
}
