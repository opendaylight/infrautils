/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.infrautils.diagstatus.web;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptySet;
import static org.opendaylight.infrautils.ready.SystemState.ACTIVE;
import static org.opendaylight.infrautils.ready.SystemState.BOOTING;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.infrautils.diagstatus.ServiceStatusSummary;
import org.opendaylight.infrautils.testutils.Partials;
import org.opendaylight.infrautils.testutils.web.TestWebServer;

/**
 * Test for {@link DiagStatusServlet}.
 *
 * @author Michael Vorburger.ch
 */
public class DiagStatusServletTest {

    private TestWebServer webServer;
    private final TestDiagStatusService testDiagStatusService = Partials.newPartial(TestDiagStatusService.class);

    @Before
    @SuppressWarnings("checkstyle:IllegalThrows") // Jetty throws Throwable
    public void beforeTest() throws Throwable {
        webServer = new TestWebServer();
        webServer.registerServlet(new DiagStatusServlet(testDiagStatusService), "/*");
    }

    @After
    public void afterTest() throws Exception {
        webServer.close();
    }

    @Test
    public void testGetWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertThat(getDiagStatusResponseCode("GET")).isEqualTo(200);
    }

    @Test
    public void testHeadWhenOk() throws IOException {
        testDiagStatusService.isOperational = true;
        assertThat(getDiagStatusResponseCode("HEAD")).isEqualTo(200);
    }

    @Test
    public void testGetWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertThat(getDiagStatusResponseCode("GET")).isEqualTo(503);
    }

    @Test
    public void testHeadWhenNok() throws IOException {
        testDiagStatusService.isOperational = false;
        assertThat(getDiagStatusResponseCode("HEAD")).isEqualTo(503);
    }

    private int getDiagStatusResponseCode(String httpMethod) throws IOException {
        URL url = new URL(webServer.getTestContextURL());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        return conn.getResponseCode();
    }

    private abstract static class TestDiagStatusService implements DiagStatusService {

        Boolean isOperational;

        @Override
        public ServiceStatusSummary getServiceStatusSummary() {
            return new ServiceStatusSummary(isOperational, isOperational ? ACTIVE : BOOTING, "", emptySet());
        }
    }
}
